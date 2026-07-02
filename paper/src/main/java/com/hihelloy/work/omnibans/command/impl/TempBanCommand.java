package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import com.hihelloy.work.omnibans.common.util.DurationParser;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TempBanCommand extends AbstractSubCommand {

    public TempBanCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "tempban";
    }

    @Override
    public String permission() {
        return "omnibans.tempban";
    }

    @Override
    public String usage() {
        return "/tempban <player> <duration> [reason]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 2) {
            usage(sender);
            return;
        }
        long expiresAt = DurationParser.parse(args[1]);
        if (expiresAt == 0L) {
            send(sender, "invalid-duration", Map.of());
            return;
        }
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "No reason specified";
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved ->
                plugin.getStaffExemptionService().hasPunishmentExemption(resolved.getUuid(), "omnibans.exempt.ban").thenAccept(exempt -> {
                    if (exempt) {
                        send(sender, "exempt.cannot-punish", Map.of("target", resolved.getName()));
                        return;
                    }
                    if (plugin.getCache().getBan(resolved.getUuid()) != null) {
                        send(sender, "exempt.already-banned", Map.of("target", resolved.getName()));
                        return;
                    }
                    String ip = resolveIp(resolved.getName());
                    plugin.getPunishmentService().ban(resolved.getUuid(), resolved.getName(), ip, staffUuid, staffName, reason, expiresAt, false).thenAccept(punishment -> {
                        if (punishment == null) {
                            return;
                        }
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("target", resolved.getName());
                        placeholders.put("reason", reason);
                        placeholders.put("duration", TimeFormatter.formatRemaining(expiresAt));
                        send(sender, "tempban.success", placeholders);
                    });
                }));
    }

    private String resolveIp(String name) {
        Player online = plugin.getServer().getPlayerExact(name);
        if (online != null && online.getAddress() != null) {
            return online.getAddress().getAddress().getHostAddress();
        }
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(onlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return filterPrefix(DURATION_SUGGESTIONS, args[1]);
        }
        if (args.length == 3) {
            return filterPrefix(reasonSuggestions(), args[2]);
        }
        return Collections.emptyList();
    }

}