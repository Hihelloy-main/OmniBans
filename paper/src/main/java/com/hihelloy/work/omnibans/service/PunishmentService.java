package com.hihelloy.work.omnibans.service;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.common.util.DurationParser;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import com.hihelloy.work.omnibans.util.PunishmentDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PunishmentService {

    private final OmniBans plugin;

    public PunishmentService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Punishment> ban(UUID targetUuid, String targetName, String targetIp, UUID staffUuid, String staffName, String reason, long expiresAt, boolean ip) {
        PunishmentType type = ip ? PunishmentType.IP_BAN : PunishmentType.BAN;
        Punishment punishment = Punishment.builder()
                .type(type)
                .scope(PunishmentScope.GLOBAL)
                .server(plugin.getOmniBansConfig().getServerName())
                .targetUuid(targetUuid)
                .targetName(targetName)
                .targetIp(targetIp)
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(reason)
                .expiresAt(expiresAt)
                .build();
        return plugin.getStorage().insert(punishment).thenApply(inserted -> {
            if (ip) {
                plugin.getCache().cacheIpBan(inserted);
            } else {
                plugin.getCache().cacheBan(inserted);
            }
            plugin.getScheduler().runGlobal(() -> {
                kickIfOnline(targetUuid, targetIp, ip, banScreen(inserted));
                broadcastBan(inserted);
            });
            publish(NetworkAction.PUNISHMENT_ADDED, inserted);
            fireWebhook(inserted);
            announceDiscord(inserted);
            return inserted;
        });
    }

    public CompletableFuture<Boolean> unban(UUID targetUuid, UUID staffUuid, String staffName, String reason) {
        return plugin.getStorage().findActiveBan(targetUuid).thenCompose(punishment -> resolveRemoval(punishment, staffUuid, staffName, reason));
    }

    public CompletableFuture<Boolean> unbanIp(String ip, UUID staffUuid, String staffName, String reason) {
        return plugin.getStorage().findActiveIpBan(ip).thenCompose(punishment -> resolveRemoval(punishment, staffUuid, staffName, reason));
    }

    public CompletableFuture<Punishment> mute(UUID targetUuid, String targetName, String targetIp, UUID staffUuid, String staffName, String reason, long expiresAt, boolean ip) {
        PunishmentType type = ip ? PunishmentType.IP_MUTE : PunishmentType.MUTE;
        Punishment punishment = Punishment.builder()
                .type(type)
                .scope(PunishmentScope.GLOBAL)
                .server(plugin.getOmniBansConfig().getServerName())
                .targetUuid(targetUuid)
                .targetName(targetName)
                .targetIp(targetIp)
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(reason)
                .expiresAt(expiresAt)
                .build();
        return plugin.getStorage().insert(punishment).thenApply(inserted -> {
            if (ip) {
                plugin.getCache().cacheIpMute(inserted);
            } else {
                plugin.getCache().cacheMute(inserted);
            }
            plugin.getScheduler().runGlobal(() -> {
                notifyMuted(inserted);
                broadcastMute(inserted);
            });
            publish(NetworkAction.PUNISHMENT_ADDED, inserted);
            fireWebhook(inserted);
            announceDiscord(inserted);
            return inserted;
        });
    }

    public CompletableFuture<Boolean> unmute(UUID targetUuid, UUID staffUuid, String staffName, String reason) {
        return plugin.getStorage().findActiveMute(targetUuid).thenCompose(punishment -> resolveRemoval(punishment, staffUuid, staffName, reason));
    }

    public CompletableFuture<Boolean> unmuteIp(String ip, UUID staffUuid, String staffName, String reason) {
        return plugin.getStorage().findActiveIpMute(ip).thenCompose(punishment -> resolveRemoval(punishment, staffUuid, staffName, reason));
    }

    public void kick(Player target, UUID staffUuid, String staffName, String reason) {
        Map<String, String> screenPlaceholders = new HashMap<>();
        screenPlaceholders.put("reason", reason);
        screenPlaceholders.put("staff", staffName != null ? staffName : "Console");
        Component message = plugin.getMessages().component("kick.screen", screenPlaceholders);
        plugin.getMessageDispatcher().kick(target, message);
        Punishment punishment = Punishment.builder()
                .type(PunishmentType.KICK)
                .scope(PunishmentScope.SERVER)
                .server(plugin.getOmniBansConfig().getServerName())
                .targetUuid(target.getUniqueId())
                .targetName(target.getName())
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(reason)
                .expiresAt(-1L)
                .active(false)
                .build();
        plugin.getStorage().insert(punishment).thenAccept(inserted -> {
            if (plugin.getOmniBansConfig().isBroadcastKick()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("target", PunishmentDisplay.safeName(inserted));
                placeholders.put("staff", staffName != null ? staffName : "Console");
                placeholders.put("reason", reason);
                plugin.getScheduler().runGlobal(() -> broadcast("kick.broadcast", placeholders));
            }
            fireWebhook(inserted);
            announceDiscord(inserted);
        });
    }

    public CompletableFuture<Integer> warn(UUID targetUuid, String targetName, UUID staffUuid, String staffName, String reason) {
        Punishment punishment = Punishment.builder()
                .type(PunishmentType.WARN)
                .scope(PunishmentScope.GLOBAL)
                .server(plugin.getOmniBansConfig().getServerName())
                .targetUuid(targetUuid)
                .targetName(targetName)
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(reason)
                .expiresAt(-1L)
                .build();
        return plugin.getStorage().insert(punishment).thenCompose(inserted -> {
            announceWarn(inserted, targetUuid, staffName, reason);
            return plugin.getStorage().countActiveWarns(targetUuid);
        }).thenApply(count -> {
            checkWarnThreshold(targetUuid, targetName, count);
            return count;
        });
    }

    public CompletableFuture<Punishment> note(UUID targetUuid, String targetName, UUID staffUuid, String staffName, String content) {
        Punishment punishment = Punishment.builder()
                .type(PunishmentType.NOTE)
                .scope(PunishmentScope.SERVER)
                .server(plugin.getOmniBansConfig().getServerName())
                .targetUuid(targetUuid)
                .targetName(targetName)
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(content)
                .expiresAt(-1L)
                .active(false)
                .build();
        return plugin.getStorage().insert(punishment);
    }

    private void announceWarn(Punishment inserted, UUID targetUuid, String staffName, String reason) {
        if (plugin.getOmniBansConfig().isBroadcastWarn()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", PunishmentDisplay.safeName(inserted));
            placeholders.put("staff", staffName != null ? staffName : "Console");
            placeholders.put("reason", reason);
            plugin.getScheduler().runGlobal(() -> broadcast("warn.broadcast", placeholders));
        }
        fireWebhook(inserted);
        plugin.getScheduler().runGlobal(() -> {
            Player online = Bukkit.getPlayer(targetUuid);
            if (online == null) {
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("staff", staffName != null ? staffName : "Console");
            placeholders.put("reason", reason);
            Component component = plugin.getMessages().component("warn.notify", placeholders);
            plugin.getMessageDispatcher().send(online, component);
        });
    }

    private void checkWarnThreshold(UUID targetUuid, String targetName, int activeWarns) {
        int threshold = plugin.getOmniBansConfig().getWarnThreshold();
        if (threshold <= 0 || activeWarns < threshold || activeWarns % threshold != 0) {
            return;
        }
        String punishmentMode = plugin.getOmniBansConfig().getWarnPunishment();
        String node = punishmentMode.equalsIgnoreCase("mute") ? "omnibans.exempt.mute" : "omnibans.exempt.ban";
        plugin.getStaffExemptionService().hasPunishmentExemption(targetUuid, node).thenAccept(exempt -> {
            if (exempt) {
                return;
            }
            applyWarnEscalation(targetUuid, targetName, activeWarns, punishmentMode);
        });
    }

    private void applyWarnEscalation(UUID targetUuid, String targetName, int activeWarns, String punishmentMode) {
        long expiresAt = DurationParser.parse(plugin.getOmniBansConfig().getWarnPunishmentDuration());
        String reason = "Automatic punishment, reached " + activeWarns + " warnings";
        if (punishmentMode.equalsIgnoreCase("mute")) {
            mute(targetUuid, targetName, null, null, "OmniBans", reason, expiresAt, false);
        } else if (punishmentMode.equalsIgnoreCase("ban")) {
            ban(targetUuid, targetName, null, null, "OmniBans", reason, -1L, false);
        } else {
            ban(targetUuid, targetName, null, null, "OmniBans", reason, expiresAt, false);
        }
    }

    private CompletableFuture<Boolean> resolveRemoval(Punishment punishment, UUID staffUuid, String staffName, String reason) {
        if (punishment == null) {
            return CompletableFuture.completedFuture(false);
        }
        punishment.setActive(false);
        punishment.setRemovedByUuid(staffUuid);
        punishment.setRemovedByName(staffName);
        punishment.setRemovedReason(reason);
        punishment.setRemovedAt(System.currentTimeMillis());
        return plugin.getStorage().update(punishment).thenApply(ignored -> {
            uncache(punishment);
            publish(NetworkAction.PUNISHMENT_REMOVED, punishment);
            return true;
        });
    }

    private void uncache(Punishment punishment) {
        switch (punishment.getType()) {
            case BAN:
                plugin.getCache().uncacheBan(punishment.getTargetUuid());
                break;
            case IP_BAN:
                plugin.getCache().uncacheIpBan(punishment.getTargetIp());
                break;
            case MUTE:
                plugin.getCache().uncacheMute(punishment.getTargetUuid());
                break;
            case IP_MUTE:
                plugin.getCache().uncacheIpMute(punishment.getTargetIp());
                break;
            default:
                break;
        }
    }

    private void kickIfOnline(UUID targetUuid, String targetIp, boolean ip, Component screen) {
        if (ip) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getAddress() != null && player.getAddress().getAddress().getHostAddress().equals(targetIp)) {
                    plugin.getMessageDispatcher().kick(player, screen);
                }
            }
            return;
        }
        Player player = Bukkit.getPlayer(targetUuid);
        if (player != null) {
            plugin.getMessageDispatcher().kick(player, screen);
        }
    }

    private Component banScreen(Punishment punishment) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
        placeholders.put("staff", punishment.getStaffName() != null ? punishment.getStaffName() : "Console");
        placeholders.put("expires", punishment.isPermanent() ? "Never" : TimeFormatter.formatRemaining(punishment.getExpiresAt()));
        placeholders.put("id", String.valueOf(punishment.getId()));
        String path = punishment.getType() == PunishmentType.IP_BAN ? "banip.screen" : (punishment.isPermanent() ? "ban.screen" : "tempban.screen");
        return plugin.getMessages().component(path, placeholders);
    }

    private void notifyMuted(Punishment punishment) {
        Player player = Bukkit.getPlayer(punishment.getTargetUuid());
        if (player == null) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
        placeholders.put("expires", punishment.isPermanent() ? "Never" : TimeFormatter.formatRemaining(punishment.getExpiresAt()));
        Component component = plugin.getMessages().component("mute.notify", placeholders);
        plugin.getMessageDispatcher().send(player, component);
    }

    private void broadcastBan(Punishment punishment) {
        if (!plugin.getOmniBansConfig().isBroadcastBan()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", PunishmentDisplay.safeName(punishment));
        placeholders.put("staff", punishment.getStaffName() != null ? punishment.getStaffName() : "Console");
        placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
        String path = punishment.getType() == PunishmentType.IP_BAN ? "banip.broadcast" : "ban.broadcast";
        broadcast(path, placeholders);
    }

    private void broadcastMute(Punishment punishment) {
        if (!plugin.getOmniBansConfig().isBroadcastMute()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", PunishmentDisplay.safeName(punishment));
        placeholders.put("staff", punishment.getStaffName() != null ? punishment.getStaffName() : "Console");
        placeholders.put("reason", punishment.getReason() != null ? punishment.getReason() : "No reason specified");
        broadcast("mute.broadcast", placeholders);
    }

    private void broadcast(String path, Map<String, String> placeholders) {
        Component component = plugin.getMessages().component(path, placeholders);
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getMessageDispatcher().send(player, component);
        }
        plugin.getMessageDispatcher().send(Bukkit.getConsoleSender(), component);
    }

    private void publish(NetworkAction action, Punishment punishment) {
        if (!plugin.getNetworkMessenger().isActive()) {
            return;
        }
        NetworkPacket packet = new NetworkPacket(
                action,
                punishment.getId(),
                punishment.getType().name(),
                punishment.getTargetUuid(),
                PunishmentDisplay.safeName(punishment),
                plugin.getOmniBansConfig().getServerName());
        plugin.getNetworkMessenger().publish(packet);
    }

    private void fireWebhook(Punishment punishment) {
        if (!plugin.getOmniBansConfig().isDiscordEnabled()) {
            return;
        }
        String title = punishment.getType().name() + " issued";
        String staff = punishment.getStaffName() != null ? punishment.getStaffName() : "Console";
        String reason = punishment.getReason() != null ? punishment.getReason() : "No reason specified";
        String description = "Target: " + PunishmentDisplay.safeName(punishment) + "\nStaff: " + staff + "\nReason: " + reason;
        plugin.getDiscordWebhook().send(title, description, 0xff0000);
    }

    private void announceDiscord(Punishment punishment) {
        String title;
        int color;
        switch (punishment.getType()) {
            case BAN:
                title = "Player banned";
                color = 0xFF0000;
                break;
            case IP_BAN:
                title = "Player ip banned";
                color = 0xFF0000;
                break;
            case MUTE:
                title = "Player muted";
                color = 0xFFA500;
                break;
            case IP_MUTE:
                title = "Player ip muted";
                color = 0xFFA500;
                break;
            case KICK:
                title = "Player kicked";
                color = 0xFFFF00;
                break;
            default:
                return;
        }
        String staff = punishment.getStaffName() != null ? punishment.getStaffName() : "Console";
        String reason = punishment.getReason() != null ? punishment.getReason() : "No reason specified";
        plugin.getDiscordAlertService().announcePunishment(title, color, punishment.getTargetUuid(), PunishmentDisplay.safeName(punishment), staff, reason);
    }

}