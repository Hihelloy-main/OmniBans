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

public final class NoteCommand extends AbstractSubCommand {

    public NoteCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "note";
    }

    @Override
    public String permission() {
        return "omnibans.note";
    }

    @Override
    public String usage() {
        return "/note <player> <note>";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 2) {
            usage(sender);
            return;
        }
        String content = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved ->
            plugin.getPunishmentService().note(resolved.getUuid(), resolved.getName(), staffUuid, staffName, content).thenAccept(punishment -> {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("target", resolved.getName());
                send(sender, "note.success", placeholders);
            }));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(knownPlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }

}
