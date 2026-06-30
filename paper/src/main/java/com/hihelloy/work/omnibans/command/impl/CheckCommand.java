package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.SeenAccount;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class CheckCommand extends AbstractSubCommand {

    public CheckCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "check";
    }

    @Override
    public String permission() {
        return "omnibans.check";
    }

    @Override
    public String usage() {
        return "/check <player>";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved -> inspect(sender, resolved.getUuid(), resolved.getName()));
    }

    private void inspect(CommandSender sender, UUID targetUuid, String targetName) {
        CompletableFuture<Punishment> banFuture = plugin.getStorage().findActiveBan(targetUuid);
        CompletableFuture<Punishment> muteFuture = plugin.getStorage().findActiveMute(targetUuid);
        CompletableFuture<Integer> warnFuture = plugin.getStorage().countActiveWarns(targetUuid);
        CompletableFuture<List<String>> ipsFuture = plugin.getStorage().findKnownIps(targetUuid);
        CompletableFuture.allOf(banFuture, muteFuture, warnFuture, ipsFuture).thenAccept(ignored -> {
            Punishment ban = banFuture.join();
            Punishment mute = muteFuture.join();
            int warns = warnFuture.join();
            List<String> ips = ipsFuture.join();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", targetName);
            placeholders.put("ban-status", ban != null && !ban.isExpired() ? "Banned, " + TimeFormatter.formatRemaining(ban.getExpiresAt()) : "Not banned");
            placeholders.put("mute-status", mute != null && !mute.isExpired() ? "Muted, " + TimeFormatter.formatRemaining(mute.getExpiresAt()) : "Not muted");
            placeholders.put("warns", String.valueOf(warns));
            placeholders.put("ip-count", String.valueOf(ips.size()));
            send(sender, "check.summary", placeholders);
            if (!ips.isEmpty()) {
                findAlts(sender, targetName, targetUuid, ips);
            }
        });
    }

    private void findAlts(CommandSender sender, String targetName, UUID targetUuid, List<String> ips) {
        List<CompletableFuture<List<SeenAccount>>> futures = new ArrayList<>();
        for (String ip : ips) {
            futures.add(plugin.getStorage().findAccountsByIp(ip));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(ignored -> {
            List<String> alts = futures.stream()
                .flatMap(future -> future.join().stream())
                .filter(account -> !account.getUuid().equals(targetUuid))
                .map(SeenAccount::getName)
                .distinct()
                .collect(Collectors.toList());
            if (alts.isEmpty()) {
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", targetName);
            placeholders.put("alts", String.join(", ", alts));
            send(sender, "check.alts", placeholders);
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(knownPlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }

}
