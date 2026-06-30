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

public final class KickCommand extends AbstractSubCommand {

    public KickCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "kick";
    }

    @Override
    public String permission() {
        return "omnibans.kick";
    }

    @Override
    public String usage() {
        return "/kick <player> [reason]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            send(sender, "player-not-found", Map.of());
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason specified";
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        plugin.getStaffExemptionService().hasPunishmentExemption(target.getUniqueId(), "omnibans.exempt.kick").thenAccept(exempt -> {
            if (exempt) {
                send(sender, "exempt.cannot-punish", Map.of("target", target.getName()));
                return;
            }
            plugin.getPunishmentService().kick(target, staffUuid, staffName, reason);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", target.getName());
            placeholders.put("reason", reason);
            send(sender, "kick.success", placeholders);
        });
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
