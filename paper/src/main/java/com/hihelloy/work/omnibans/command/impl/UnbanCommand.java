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

public final class UnbanCommand extends AbstractSubCommand {

    public UnbanCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unban";
    }

    @Override
    public String permission() {
        return "omnibans.unban";
    }

    @Override
    public String usage() {
        return "/unban <player>";
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
            plugin.getPunishmentService().unban(resolved.getUuid(), staffUuid, staffName, "Unbanned").thenAccept(success -> {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("target", resolved.getName());
                send(sender, success ? "unban.success" : "unban.not-banned", placeholders);
            }));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(bannedPlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }

}
