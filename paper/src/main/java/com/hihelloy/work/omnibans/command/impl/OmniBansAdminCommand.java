package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class OmniBansAdminCommand extends AbstractSubCommand {

    public OmniBansAdminCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "omnibans";
    }

    @Override
    public String permission() {
        return "omnibans.admin";
    }

    @Override
    public String usage() {
        return "/omnibans <reload|version|config|messages|spy>";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            send(sender, "admin.reloaded", Map.of());
            return;
        }
        if (args[0].equalsIgnoreCase("version")) {
            send(sender, "admin.version", Map.of("version", plugin.getDescription().getVersion()));
            return;
        }
        if (args[0].equalsIgnoreCase("config")) {
            openEditor(sender, args, true);
            return;
        }
        if (args[0].equalsIgnoreCase("messages")) {
            openEditor(sender, args, false);
            return;
        }
        if (args[0].equalsIgnoreCase("spy")) {
            boolean newValue = !plugin.getOmniBansConfig().isSpyAttempts();
            plugin.getConfig().set("alerts.spy-attempts", newValue);
            plugin.saveConfig();
            plugin.getOmniBansConfig().load();
            send(sender, "alerts.spy-toggled", Map.of("state", newValue ? "enabled" : "disabled"));
            return;
        }
        usage(sender);
    }

    private void openEditor(CommandSender sender, String[] args, boolean config) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only an in game player can open the config editor.");
            return;
        }
        int page = parsePage(args);
        if (config) {
            plugin.getConfigGuiService().openConfig(player, page);
        } else {
            plugin.getConfigGuiService().openMessages(player, page);
        }
    }

    private int parsePage(String[] args) {
        if (args.length < 2) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(args[1]) - 1);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("reload", "version", "config", "messages", "spy"), args[0]);
        }
        return Collections.emptyList();
    }

}
