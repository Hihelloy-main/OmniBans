package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class AltsCommand extends AbstractSubCommand {

    public AltsCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "alts";
    }

    @Override
    public String permission() {
        return "omnibans.alts";
    }

    @Override
    public String usage() {
        return "/alts <player>";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        plugin.getPlayerResolver().resolve(args[0]).thenAccept(resolved ->
            plugin.getStaffExemptionService().isExempt(resolved.getUuid()).thenAccept(exempt -> {
                if (exempt) {
                    send(sender, "alts.exempt", Map.of("target", resolved.getName()));
                    return;
                }
                inspect(sender, resolved.getUuid(), resolved.getName());
            }));
    }

    private void inspect(CommandSender sender, UUID targetUuid, String targetName) {
        plugin.getAltLookupService().findAlts(targetUuid).thenAccept(altMap -> {
            if (altMap.isEmpty()) {
                send(sender, "alts.none", Map.of("target", targetName));
                return;
            }
            displayAlts(sender, targetUuid, targetName, altMap);
        });
    }

    private void displayAlts(CommandSender sender, UUID targetUuid, String targetName, Map<UUID, String> altMap) {
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("target", targetName);
        headerPlaceholders.put("count", String.valueOf(altMap.size()));
        send(sender, "alts.header", headerPlaceholders);
        for (Map.Entry<UUID, String> entry : altMap.entrySet()) {
            describeAlt(sender, entry.getKey(), entry.getValue());
        }
        plugin.getStorage().findKnownIps(targetUuid).thenAccept(ips -> describeSharedIpStatus(sender, ips));
    }

    private void describeAlt(CommandSender sender, UUID altUuid, String altName) {
        CompletableFuture<Punishment> banFuture = plugin.getStorage().findActiveBan(altUuid);
        CompletableFuture<Punishment> muteFuture = plugin.getStorage().findActiveMute(altUuid);
        CompletableFuture.allOf(banFuture, muteFuture).thenAccept(ignored -> {
            boolean banned = isActive(banFuture.join());
            boolean muted = isActive(muteFuture.join());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", altName);
            placeholders.put("status", formatStatus(banned, muted));
            send(sender, "alts.entry", placeholders);
        });
    }

    private void describeSharedIpStatus(CommandSender sender, List<String> ips) {
        List<CompletableFuture<Punishment>> ipBanFutures = new ArrayList<>();
        List<CompletableFuture<Punishment>> ipMuteFutures = new ArrayList<>();
        for (String ip : ips) {
            ipBanFutures.add(plugin.getStorage().findActiveIpBan(ip));
            ipMuteFutures.add(plugin.getStorage().findActiveIpMute(ip));
        }
        List<CompletableFuture<Punishment>> combined = new ArrayList<>();
        combined.addAll(ipBanFutures);
        combined.addAll(ipMuteFutures);
        CompletableFuture.allOf(combined.toArray(new CompletableFuture[0])).thenAccept(ignored -> {
            boolean ipBanned = ipBanFutures.stream().anyMatch(future -> isActive(future.join()));
            boolean ipMuted = ipMuteFutures.stream().anyMatch(future -> isActive(future.join()));
            if (!ipBanned && !ipMuted) {
                return;
            }
            send(sender, "alts.shared-ip-status", Map.of("status", formatIpStatus(ipBanned, ipMuted)));
        });
    }

    private boolean isActive(Punishment punishment) {
        return punishment != null && !punishment.isExpired();
    }

    private String formatStatus(boolean banned, boolean muted) {
        if (banned && muted) {
            return "<red>[Banned, Muted]";
        }
        if (banned) {
            return "<red>[Banned]";
        }
        if (muted) {
            return "<gold>[Muted]";
        }
        return "<green>[Clean]";
    }

    private String formatIpStatus(boolean ipBanned, boolean ipMuted) {
        if (ipBanned && ipMuted) {
            return "<red>IP banned and IP muted";
        }
        if (ipBanned) {
            return "<red>IP banned";
        }
        return "<gold>IP muted";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(knownPlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }

}
