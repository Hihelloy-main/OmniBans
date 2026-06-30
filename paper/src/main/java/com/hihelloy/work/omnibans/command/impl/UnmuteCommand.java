package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class UnmuteCommand extends AbstractSubCommand {

    public UnmuteCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unmute";
    }

    @Override
    public String permission() {
        return "omnibans.unmute";
    }

    @Override
    public String usage() {
        return "/unmute <player>";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved ->
            plugin.getPunishmentService().unmute(resolved.getUuid(), staffUuid, staffName, "Unmuted").thenAccept(success -> {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("target", resolved.getName());
                send(sender, success ? "unmute.success" : "unmute.not-muted", placeholders);
            }));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(mutedPlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }

}
