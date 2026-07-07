package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.util.PunishmentDisplay;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MuteListCommand extends AbstractSubCommand {

    private static final int PAGE_SIZE = 10;

    public MuteListCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "mutelist";
    }

    @Override
    public String permission() {
        return "omnibans.mutelist";
    }

    @Override
    public String usage() {
        return "/mutelist [page]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        int page = args.length > 0 ? parsePage(args[0]) : 1;
        plugin.getStorage().findActiveByType(PunishmentType.MUTE).thenAccept(mutes -> sendPage(sender, mutes, page));
    }

    private int parsePage(String input) {
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private void sendPage(CommandSender sender, List<Punishment> mutes, int page) {
        if (mutes.isEmpty()) {
            send(sender, "mutelist.empty", Map.of());
            return;
        }
        int totalPages = Math.max(1, (int) Math.ceil(mutes.size() / (double) PAGE_SIZE));
        int boundedPage = Math.min(page, totalPages);
        int start = (boundedPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, mutes.size());
        List<Punishment> slice = new ArrayList<>(mutes.subList(start, end));
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("page", String.valueOf(boundedPage));
        headerPlaceholders.put("total", String.valueOf(totalPages));
        headerPlaceholders.put("count", String.valueOf(mutes.size()));
        send(sender, "mutelist.header", headerPlaceholders);
        for (Punishment punishment : slice) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", PunishmentDisplay.safeName(punishment));
            placeholders.put("staff", punishment.getStaffName() != null ? punishment.getStaffName() : "Console");
            placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
            send(sender, "mutelist.entry", placeholders);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(PAGE_SUGGESTIONS, args[0]);
        }
        return Collections.emptyList();
    }

}
