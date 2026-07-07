package com.hihelloy.work.omnibans.forge.command;

import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;
import com.hihelloy.work.omnibans.api.request.PunishmentRequest;
import com.hihelloy.work.omnibans.common.mod.ModBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.common.util.DurationParser;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class BrigadierCommandRegistry {

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    private BrigadierCommandRegistry() {
    }

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher, ModBans mod) {
        registerBan(dispatcher, mod);
        registerTempBan(dispatcher, mod);
        registerUnban(dispatcher, mod);
        registerBanIp(dispatcher, mod);
        registerUnbanIp(dispatcher, mod);
        registerMute(dispatcher, mod);
        registerTempMute(dispatcher, mod);
        registerUnmute(dispatcher, mod);
        registerKick(dispatcher, mod);
        registerWarn(dispatcher, mod);
        registerNote(dispatcher, mod);
        registerHistory(dispatcher, mod);
        registerCheck(dispatcher, mod);
        registerBanList(dispatcher, mod);
        registerMuteList(dispatcher, mod);
        registerAlts(dispatcher, mod);
        registerOmniBans(dispatcher, mod);
    }

    private static void registerBan(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("ban").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> execBan(ctx, mod, "No reason specified", -1L))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> execBan(ctx, mod, StringArgumentType.getString(ctx, "reason"), -1L)))));
    }

    private static void registerTempBan(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("tempban").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .then(Commands.argument("duration", StringArgumentType.word())
                    .executes(ctx -> execBan(ctx, mod, "No reason specified", DurationParser.parse(StringArgumentType.getString(ctx, "duration"))))
                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(ctx -> execBan(ctx, mod, StringArgumentType.getString(ctx, "reason"), DurationParser.parse(StringArgumentType.getString(ctx, "duration"))))))));
    }

    private static int execBan(CommandContext<CommandSourceStack> ctx, ModBans mod, String reason, long expiresAt) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
        String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
        String ip = target != null ? ((InetSocketAddress) target.connection.getRemoteAddress()).getAddress().getHostAddress() : null;
        mod.getPunishmentManager().ban(PunishmentRequest.builder(ApiPunishmentType.BAN)
            .target(target != null ? target.getUUID() : null, name)
            .targetIp(ip != null ? ip : "")
            .staff(staffUuid, staffName).reason(reason).expiresAt(expiresAt).build()).thenAccept(p -> {
            if (p != null && target != null) {
                target.connection.disconnect(Component.literal("You are banned.\nReason: " + reason));
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Banned " + name + " for: " + reason), true);
        });
        return 1;
    }

    private static void registerUnban(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("unban").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "player");
                    ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                    UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
                    String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
                    mod.getPunishmentManager().unban(target != null ? target.getUUID() : null, staffUuid, staffName, "Unbanned").thenAccept(ok ->
                        ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Unbanned " + name : name + " is not banned."), true));
                    return 1;
                })));
    }

    private static void registerBanIp(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("banip").requires(s -> s.hasPermission(2))
            .then(Commands.argument("target", StringArgumentType.word())
                .executes(ctx -> execBanIp(ctx, mod, "No reason specified"))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> execBanIp(ctx, mod, StringArgumentType.getString(ctx, "reason"))))));
    }

    private static int execBanIp(CommandContext<CommandSourceStack> ctx, ModBans mod, String reason) {
        String input = StringArgumentType.getString(ctx, "target");
        UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
        String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
        if (IP_PATTERN.matcher(input).matches()) {
            mod.getPunishmentManager().ban(PunishmentRequest.builder(ApiPunishmentType.IP_BAN)
                .targetIp(input).staff(staffUuid, staffName).reason(reason).permanent().build()).thenAccept(p ->
                ctx.getSource().sendSuccess(() -> Component.literal("IP banned."), true));
            return 1;
        }
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(input);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("That player is not online."));
            return 0;
        }
        String ip = ((InetSocketAddress) target.connection.getRemoteAddress()).getAddress().getHostAddress();
        mod.getPunishmentManager().ban(PunishmentRequest.builder(ApiPunishmentType.IP_BAN)
            .target(target.getUUID(), input).targetIp(ip)
            .staff(staffUuid, staffName).reason(reason).permanent().build()).thenAccept(p -> {
            if (p != null) {
                target.connection.disconnect(Component.literal("Your IP has been banned.\nReason: " + reason));
            }
            ctx.getSource().sendSuccess(() -> Component.literal("IP banned " + input + "."), true);
        });
        return 1;
    }

    private static void registerUnbanIp(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("unbanip").requires(s -> s.hasPermission(2))
            .then(Commands.argument("target", StringArgumentType.word())
                .executes(ctx -> {
                    String input = StringArgumentType.getString(ctx, "target");
                    UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
                    String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
                    if (IP_PATTERN.matcher(input).matches()) {
                        mod.getPunishmentManager().unbanIp(input, staffUuid, staffName, "Unbanned").thenAccept(ok ->
                            ctx.getSource().sendSuccess(() -> Component.literal(ok ? "IP unbanned." : "That IP is not banned."), true));
                        return 1;
                    }
                    ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(input);
                    if (target == null) {
                        ctx.getSource().sendFailure(Component.literal("That player is not online."));
                        return 0;
                    }
                    mod.getStorage().findKnownIps(target.getUUID()).thenAccept(ips -> {
                        if (ips.isEmpty()) {
                            ctx.getSource().sendFailure(Component.literal("No known IP for " + input));
                            return;
                        }
                        tryUnbanIps(ctx, mod, input, ips, 0, staffUuid, staffName);
                    });
                    return 1;
                })));
    }

    private static void tryUnbanIps(CommandContext<CommandSourceStack> ctx, ModBans mod, String name, List<String> ips, int idx, UUID staff, String staffName) {
        if (idx >= ips.size()) {
            ctx.getSource().sendSuccess(() -> Component.literal(name + " has no active IP ban."), true);
            return;
        }
        mod.getPunishmentManager().unbanIp(ips.get(idx), staff, staffName, "Unbanned").thenAccept(ok -> {
            if (ok) {
                ctx.getSource().sendSuccess(() -> Component.literal("IP unbanned " + name + "."), true);
            } else {
                tryUnbanIps(ctx, mod, name, ips, idx + 1, staff, staffName);
            }
        });
    }

    private static void registerMute(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("mute").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> execMute(ctx, mod, "No reason specified", -1L))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> execMute(ctx, mod, StringArgumentType.getString(ctx, "reason"), -1L)))));
    }

    private static void registerTempMute(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("tempmute").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .then(Commands.argument("duration", StringArgumentType.word())
                    .executes(ctx -> execMute(ctx, mod, "No reason specified", DurationParser.parse(StringArgumentType.getString(ctx, "duration"))))
                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                        .executes(ctx -> execMute(ctx, mod, StringArgumentType.getString(ctx, "reason"), DurationParser.parse(StringArgumentType.getString(ctx, "duration"))))))));
    }

    private static int execMute(CommandContext<CommandSourceStack> ctx, ModBans mod, String reason, long expiresAt) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("That player is not online."));
            return 0;
        }
        UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
        String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
        mod.getPunishmentManager().mute(PunishmentRequest.builder(ApiPunishmentType.MUTE)
            .target(target.getUUID(), name).staff(staffUuid, staffName).reason(reason).expiresAt(expiresAt).build()).thenAccept(p -> {
            if (p != null) {
                target.sendSystemMessage(Component.literal("You have been muted. Reason: " + reason));
                String msg = expiresAt == -1L ? "Muted " + name + " permanently: " + reason
                    : "Muted " + name + " for " + TimeFormatter.formatRemaining(expiresAt) + ": " + reason;
                ctx.getSource().sendSuccess(() -> Component.literal(msg), true);
            }
        });
        return 1;
    }

    private static void registerUnmute(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("unmute").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "player");
                    ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                    if (target == null) {
                        ctx.getSource().sendFailure(Component.literal("That player is not online."));
                        return 0;
                    }
                    UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
                    String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
                    mod.getPunishmentManager().unmute(target.getUUID(), staffUuid, staffName, "Unmuted").thenAccept(ok ->
                        ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Unmuted " + name : name + " is not muted."), true));
                    return 1;
                })));
    }

    private static void registerKick(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("kick").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> execKick(ctx, "No reason specified"))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> execKick(ctx, StringArgumentType.getString(ctx, "reason"))))));
    }

    private static int execKick(CommandContext<CommandSourceStack> ctx, String reason) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("That player is not online."));
            return 0;
        }
        target.connection.disconnect(Component.literal("Kicked: " + reason));
        ctx.getSource().sendSuccess(() -> Component.literal("Kicked " + name + ": " + reason), true);
        return 1;
    }

    private static void registerWarn(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("warn").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> execWarn(ctx, mod, "No reason specified"))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> execWarn(ctx, mod, StringArgumentType.getString(ctx, "reason"))))));
    }

    private static int execWarn(CommandContext<CommandSourceStack> ctx, ModBans mod, String reason) {
        String name = StringArgumentType.getString(ctx, "player");
        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("That player is not online."));
            return 0;
        }
        UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
        String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
        mod.getPunishmentManager().warn(PunishmentRequest.builder(ApiPunishmentType.WARN)
            .target(target.getUUID(), name).staff(staffUuid, staffName).reason(reason).build()).thenAccept(p -> {
            if (p != null) {
                target.sendSystemMessage(Component.literal("You have been warned by " + staffName + ". Reason: " + reason));
                ctx.getSource().sendSuccess(() -> Component.literal("Warned " + name + ": " + reason), true);
            }
        });
        return 1;
    }

    private static void registerNote(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("note").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .then(Commands.argument("content", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String name = StringArgumentType.getString(ctx, "player");
                        String content = StringArgumentType.getString(ctx, "content");
                        ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                        UUID staffUuid = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getUUID() : null;
                        String staffName = ctx.getSource().isPlayer() ? ctx.getSource().getPlayer().getGameProfile().getName() : "Console";
                        mod.getPunishmentManager().note(PunishmentRequest.builder(ApiPunishmentType.NOTE)
                            .target(target != null ? target.getUUID() : null, name)
                            .staff(staffUuid, staffName).reason(content).build()).thenAccept(p ->
                            ctx.getSource().sendSuccess(() -> Component.literal("Note added for " + name + "."), true));
                        return 1;
                    }))));
    }

    private static void registerHistory(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("history").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "player");
                    ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                    if (target == null) {
                        ctx.getSource().sendFailure(Component.literal("That player is not online."));
                        return 0;
                    }
                    mod.getPunishmentManager().getHistory(target.getUUID()).thenAccept(list -> {
                        ctx.getSource().sendSuccess(() -> Component.literal("--- History for " + name + " (" + list.size() + ") ---"), false);
                        int shown = Math.min(list.size(), 10);
                        for (int i = 0; i < shown; i++) {
                            var p = list.get(list.size() - 1 - i);
                            String entry = "#" + p.getId() + " " + p.getType().name() + " by " + nvl(p.getStaffName()) + " - " + nvl(p.getReason());
                            ctx.getSource().sendSuccess(() -> Component.literal(entry), false);
                        }
                        if (list.size() > 10) {
                            ctx.getSource().sendSuccess(() -> Component.literal("... and " + (list.size() - 10) + " more."), false);
                        }
                    });
                    return 1;
                })));
    }

    private static void registerCheck(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("check").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "player");
                    ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                    if (target == null) {
                        ctx.getSource().sendFailure(Component.literal("That player is not online."));
                        return 0;
                    }
                    UUID uuid = target.getUUID();
                    ctx.getSource().sendSuccess(() -> Component.literal("--- Check: " + name + " ---"), false);
                    mod.getPunishmentManager().getActiveBan(uuid).thenAccept(ban ->
                        ctx.getSource().sendSuccess(() -> Component.literal("Ban: " + ban.map(b -> nvl(b.getReason())).orElse("None")), false));
                    mod.getPunishmentManager().getActiveMute(uuid).thenAccept(mute ->
                        ctx.getSource().sendSuccess(() -> Component.literal("Mute: " + mute.map(m -> nvl(m.getReason())).orElse("None")), false));
                    mod.getPunishmentManager().getActiveWarnCount(uuid).thenAccept(count ->
                        ctx.getSource().sendSuccess(() -> Component.literal("Active warnings: " + count), false));
                    return 1;
                })));
    }

    private static void registerBanList(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("banlist").requires(s -> s.hasPermission(2))
            .executes(ctx -> {
                mod.getStorage().findActiveByType(PunishmentType.BAN).thenAccept(list -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("--- Ban List (" + list.size() + ") ---"), false);
                    list.stream().limit(20).forEach(p ->
                        ctx.getSource().sendSuccess(() -> Component.literal(nvl(p.getTargetName()) + " - " + nvl(p.getReason())), false));
                    if (list.size() > 20) {
                        ctx.getSource().sendSuccess(() -> Component.literal("... and " + (list.size() - 20) + " more."), false);
                    }
                });
                return 1;
            }));
    }

    private static void registerMuteList(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("mutelist").requires(s -> s.hasPermission(2))
            .executes(ctx -> {
                mod.getStorage().findActiveByType(PunishmentType.MUTE).thenAccept(list -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("--- Mute List (" + list.size() + ") ---"), false);
                    list.stream().limit(20).forEach(p ->
                        ctx.getSource().sendSuccess(() -> Component.literal(nvl(p.getTargetName()) + " - " + nvl(p.getReason())), false));
                    if (list.size() > 20) {
                        ctx.getSource().sendSuccess(() -> Component.literal("... and " + (list.size() - 20) + " more."), false);
                    }
                });
                return 1;
            }));
    }

    private static void registerAlts(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("alts").requires(s -> s.hasPermission(2))
            .then(Commands.argument("player", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "player");
                    ServerPlayer target = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                    if (target == null) {
                        ctx.getSource().sendFailure(Component.literal("That player is not online."));
                        return 0;
                    }
                    mod.getPlayerManager().getAlts(target.getUUID()).thenAccept(alts -> {
                        if (alts.isEmpty()) {
                            ctx.getSource().sendSuccess(() -> Component.literal(name + " has no known alts."), false);
                            return;
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal("--- Alts for " + name + " (" + alts.size() + ") ---"), false);
                        alts.forEach(alt -> ctx.getSource().sendSuccess(() -> Component.literal(alt.getName()), false));
                    });
                    return 1;
                })));
    }

    private static void registerOmniBans(CommandDispatcher<CommandSourceStack> d, ModBans mod) {
        d.register(Commands.literal("omnibans").requires(s -> s.hasPermission(3))
            .then(Commands.literal("version")
                .executes(ctx -> { ctx.getSource().sendSuccess(() -> Component.literal("OmniBans v1.0"), false); return 1; }))
            .then(Commands.literal("reload")
                .executes(ctx -> { mod.reload(); ctx.getSource().sendSuccess(() -> Component.literal("OmniBans configuration reloaded."), false); return 1; }))
            .then(Commands.literal("spy")
                .executes(ctx -> {
                    boolean val = !mod.isSpyAttempts();
                    mod.setSpyAttempts(val);
                    ctx.getSource().sendSuccess(() -> Component.literal("Punishment attempt alerts " + (val ? "enabled" : "disabled") + "."), false);
                    return 1;
                })));
    }

    private static String nvl(String s) {
        return s != null ? s : "Unknown";
    }

}
