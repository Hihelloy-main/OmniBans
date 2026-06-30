package com.hihelloy.work.omnibans.task;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class SyncTask {

    private final OmniBans plugin;
    private volatile boolean running;

    public SyncTask(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void start() {
        running = true;
        plugin.getScheduler().runGlobalRepeating(this::sync, 200L, 200L);
    }

    public void stop() {
        running = false;
    }

    private void sync() {
        if (!running) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            syncBan(player);
            syncMute(player);
        }
    }

    private void syncBan(Player player) {
        plugin.getStorage().findActiveBan(player.getUniqueId()).thenAccept(ban -> {
            if (ban == null || ban.isExpired()) {
                return;
            }
            plugin.getCache().cacheBan(ban);
            plugin.getScheduler().runGlobal(() -> kickForBan(player, ban));
        });
    }

    private void syncMute(Player player) {
        plugin.getStorage().findActiveMute(player.getUniqueId()).thenAccept(mute -> {
            if (mute != null && !mute.isExpired()) {
                plugin.getCache().cacheMute(mute);
            } else {
                plugin.getCache().uncacheMute(player.getUniqueId());
            }
        });
    }

    private void kickForBan(Player player, Punishment ban) {
        if (!player.isOnline()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", ban.getReason() != null ? ban.getReason() : "No reason specified");
        placeholders.put("staff", ban.getStaffName() != null ? ban.getStaffName() : "Console");
        placeholders.put("expires", ban.isPermanent() ? "Never" : TimeFormatter.formatRemaining(ban.getExpiresAt()));
        placeholders.put("id", String.valueOf(ban.getId()));
        Component component = plugin.getMessages().component("ban.screen", placeholders);
        plugin.getMessageDispatcher().kick(player, component);
    }

}
