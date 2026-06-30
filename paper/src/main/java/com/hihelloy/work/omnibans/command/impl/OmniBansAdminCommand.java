package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;

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
        return "/omnibans <reload|version>";
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
        usage(sender);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("reload", "version"), args[0]);
        }
        return Collections.emptyList();
    }

}
