package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BanListCommand extends AbstractSubCommand {

    private static final int PAGE_SIZE = 10;

    public BanListCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "banlist";
    }

    @Override
    public String permission() {
        return "omnibans.banlist";
    }

    @Override
    public String usage() {
        return "/banlist [page]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        int page = args.length > 0 ? parsePage(args[0]) : 1;
        plugin.getStorage().findActiveByType(PunishmentType.BAN).thenAccept(bans -> sendPage(sender, bans, page));
    }

    private int parsePage(String input) {
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private void sendPage(CommandSender sender, List<Punishment> bans, int page) {
        if (bans.isEmpty()) {
            send(sender, "banlist.empty", Map.of());
            return;
        }
        int totalPages = Math.max(1, (int) Math.ceil(bans.size() / (double) PAGE_SIZE));
        int boundedPage = Math.min(page, totalPages);
        int start = (boundedPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, bans.size());
        List<Punishment> slice = new ArrayList<>(bans.subList(start, end));
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("page", String.valueOf(boundedPage));
        headerPlaceholders.put("total", String.valueOf(totalPages));
        headerPlaceholders.put("count", String.valueOf(bans.size()));
        send(sender, "banlist.header", headerPlaceholders);
        for (Punishment punishment : slice) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", punishment.getTargetName());
            placeholders.put("staff", punishment.getStaffName() != null ? punishment.getStaffName() : "Console");
            placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
            send(sender, "banlist.entry", placeholders);
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
