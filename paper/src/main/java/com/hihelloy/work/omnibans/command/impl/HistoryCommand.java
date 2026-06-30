package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HistoryCommand extends AbstractSubCommand {

    private static final int PAGE_SIZE = 8;

    public HistoryCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "history";
    }

    @Override
    public String permission() {
        return "omnibans.history";
    }

    @Override
    public String usage() {
        return "/history <player> [page]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        int page = args.length > 1 ? parsePage(args[1]) : 1;
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved ->
            plugin.getStorage().history(resolved.getUuid()).thenAccept(history -> {
                if (history.isEmpty()) {
                    send(sender, "history.empty", Map.of("target", resolved.getName()));
                    return;
                }
                sendPage(sender, resolved.getName(), history, page);
            }));
    }

    private int parsePage(String input) {
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private void sendPage(CommandSender sender, String targetName, List<Punishment> history, int page) {
        int totalPages = Math.max(1, (int) Math.ceil(history.size() / (double) PAGE_SIZE));
        int boundedPage = Math.min(page, totalPages);
        int start = (boundedPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, history.size());
        List<Punishment> slice = new ArrayList<>(history.subList(start, end));
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("target", targetName);
        headerPlaceholders.put("page", String.valueOf(boundedPage));
        headerPlaceholders.put("total", String.valueOf(totalPages));
        send(sender, "history.header", headerPlaceholders);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm");
        for (Punishment punishment : slice) {
            sendEntry(sender, punishment, dateFormat);
        }
    }

    private void sendEntry(CommandSender sender, Punishment punishment, SimpleDateFormat dateFormat) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("id", String.valueOf(punishment.getId()));
        placeholders.put("type", punishment.getType().name());
        placeholders.put("staff", punishment.getStaffName() != null ? punishment.getStaffName() : "Console");
        placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
        placeholders.put("date", dateFormat.format(new Date(punishment.getCreatedAt())));
        placeholders.put("active", punishment.isActive() && !punishment.isExpired() ? "Active" : "Inactive");
        placeholders.put("expires", punishment.isPermanent() ? "Never" : TimeFormatter.formatRemaining(punishment.getExpiresAt()));
        send(sender, "history.entry", placeholders);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(knownPlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return filterPrefix(PAGE_SUGGESTIONS, args[1]);
        }
        return Collections.emptyList();
    }

}
