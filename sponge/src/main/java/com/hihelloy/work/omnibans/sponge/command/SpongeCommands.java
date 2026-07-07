package com.hihelloy.work.omnibans.sponge.command;

import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;
import com.hihelloy.work.omnibans.api.request.PunishmentRequest;
import com.hihelloy.work.omnibans.common.util.DurationParser;
import com.hihelloy.work.omnibans.sponge.OmniBansSponge;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

import java.util.UUID;

public final class SpongeCommands {

    private SpongeCommands() {
    }

    public static void register(RegisterCommandEvent.Parameterized event, OmniBansSponge plugin) {
        Parameter.Value<String> playerParam = Parameter.string().key("player").build();
        Parameter.Value<String> reasonParam = Parameter.remainingJoinedStrings().key("reason").optional().build();
        Parameter.Value<String> durationParam = Parameter.string().key("duration").build();

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.ban")
            .addParameter(playerParam)
            .addParameter(reasonParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                String reason = ctx.one(reasonParam).orElse("No reason specified");
                banPlayer(plugin, ctx.cause().audience(), name, reason, -1L);
                return CommandResult.success();
            }).build(), "ban");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.tempban")
            .addParameter(playerParam)
            .addParameter(durationParam)
            .addParameter(reasonParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                long expires = DurationParser.parse(ctx.requireOne(durationParam));
                String reason = ctx.one(reasonParam).orElse("No reason specified");
                banPlayer(plugin, ctx.cause().audience(), name, reason, expires);
                return CommandResult.success();
            }).build(), "tempban");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.mute")
            .addParameter(playerParam)
            .addParameter(reasonParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                String reason = ctx.one(reasonParam).orElse("No reason specified");
                mutePlayer(plugin, ctx.cause().audience(), name, reason, -1L);
                return CommandResult.success();
            }).build(), "mute");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.tempmute")
            .addParameter(playerParam)
            .addParameter(durationParam)
            .addParameter(reasonParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                long expires = DurationParser.parse(ctx.requireOne(durationParam));
                String reason = ctx.one(reasonParam).orElse("No reason specified");
                mutePlayer(plugin, ctx.cause().audience(), name, reason, expires);
                return CommandResult.success();
            }).build(), "tempmute");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.unmute")
            .addParameter(playerParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                plugin.getServer().flatMap(s -> s.player(name)).ifPresent(target -> {
                    UUID staffUuid = ctx.cause().first(ServerPlayer.class).map(ServerPlayer::uniqueId).orElse(null);
                    String staffName = ctx.cause().first(ServerPlayer.class).map(ServerPlayer::name).orElse("Console");
                    plugin.getPunishmentManager().unmute(target.uniqueId(), staffUuid, staffName, "Unmuted").thenAccept(ok ->
                        ctx.cause().audience().sendMessage(Component.text(ok ? "Unmuted " + name : name + " is not muted.")));
                });
                return CommandResult.success();
            }).build(), "unmute");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.kick")
            .addParameter(playerParam)
            .addParameter(reasonParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                String reason = ctx.one(reasonParam).orElse("No reason specified");
                plugin.getServer().flatMap(s -> s.player(name)).ifPresent(target -> {
                    target.kick(Component.text("Kicked: " + reason));
                    ctx.cause().audience().sendMessage(Component.text("Kicked " + name + ": " + reason));
                });
                return CommandResult.success();
            }).build(), "kick");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.warn")
            .addParameter(playerParam)
            .addParameter(reasonParam)
            .executor(ctx -> {
                String name = ctx.requireOne(playerParam);
                String reason = ctx.one(reasonParam).orElse("No reason specified");
                plugin.getServer().flatMap(s -> s.player(name)).ifPresent(target -> {
                    UUID staffUuid = ctx.cause().first(ServerPlayer.class).map(ServerPlayer::uniqueId).orElse(null);
                    String staffName = ctx.cause().first(ServerPlayer.class).map(ServerPlayer::name).orElse("Console");
                    plugin.getPunishmentManager().warn(PunishmentRequest.builder(ApiPunishmentType.WARN)
                        .target(target.uniqueId(), name).staff(staffUuid, staffName).reason(reason).build()).thenAccept(p ->
                        ctx.cause().audience().sendMessage(Component.text("Warned " + name + ": " + reason)));
                });
                return CommandResult.success();
            }).build(), "warn");

        event.register(plugin.container(), Command.builder()
            .permission("omnibans.admin")
            .addChild(Command.builder().executor(ctx -> {
                ctx.cause().audience().sendMessage(Component.text("OmniBans v1.0 running on Sponge"));
                return CommandResult.success();
            }).build(), "version")
            .addChild(Command.builder().executor(ctx -> {
                plugin.reload();
                ctx.cause().audience().sendMessage(Component.text("OmniBans configuration reloaded."));
                return CommandResult.success();
            }).build(), "reload")
            .addChild(Command.builder().executor(ctx -> {
                boolean newValue = !plugin.isSpyAttempts();
                plugin.setSpyAttempts(newValue);
                ctx.cause().audience().sendMessage(Component.text("Punishment attempt alerts " + (newValue ? "enabled" : "disabled") + "."));
                return CommandResult.success();
            }).build(), "spy")
            .build(), "omnibans");
    }

    private static void banPlayer(OmniBansSponge plugin, net.kyori.adventure.audience.Audience sender, String name, String reason, long expires) {
        plugin.getServer().flatMap(s -> s.player(name)).ifPresent(target -> {
            plugin.getPunishmentManager().ban(PunishmentRequest.builder(ApiPunishmentType.BAN)
                .target(target.uniqueId(), name)
                .staff(null, "Console")
                .reason(reason).expiresAt(expires).build()).thenAccept(p -> {
                if (p != null) {
                    target.kick(Component.text("You are banned.\nReason: " + reason));
                }
                sender.sendMessage(Component.text("Banned " + name + " for: " + reason));
            });
        });
    }

    private static void mutePlayer(OmniBansSponge plugin, net.kyori.adventure.audience.Audience sender, String name, String reason, long expires) {
        plugin.getServer().flatMap(s -> s.player(name)).ifPresent(target -> {
            plugin.getPunishmentManager().mute(PunishmentRequest.builder(ApiPunishmentType.MUTE)
                .target(target.uniqueId(), name)
                .staff(null, "Console")
                .reason(reason).expiresAt(expires).build()).thenAccept(p -> {
                if (p != null) {
                    target.sendMessage(Component.text("You have been muted. Reason: " + reason));
                }
                sender.sendMessage(Component.text("Muted " + name + " for: " + reason));
            });
        });
    }

}
