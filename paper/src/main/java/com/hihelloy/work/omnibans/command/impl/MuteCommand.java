package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MuteCommand extends AbstractSubCommand {

    public MuteCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mute";
    }

    @Override
    public String permission() {
        return "omnibans.mute";
    }

    @Override
    public String usage() {
        return "/mute <player> [reason]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason specified";
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved ->
            plugin.getStaffExemptionService().hasPunishmentExemption(resolved.getUuid(), "omnibans.exempt.mute").thenAccept(exempt -> {
                if (exempt) {
                    send(sender, "exempt.cannot-punish", Map.of("target", resolved.getName()));
                    return;
                }
                if (plugin.getCache().getMute(resolved.getUuid()) != null) {
                    send(sender, "exempt.already-muted", Map.of("target", resolved.getName()));
                    return;
                }
                plugin.getPunishmentService().mute(resolved.getUuid(), resolved.getName(), null, staffUuid, staffName, reason, -1L, false).thenAccept(punishment -> {
                    if (punishment == null) {
                        return;
                    }
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("target", resolved.getName());
                    placeholders.put("reason", reason);
                    send(sender, "mute.success", placeholders);
                });
            }));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(onlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return filterPrefix(reasonSuggestions(), args[1]);
        }
        return Collections.emptyList();
    }

}
