package com.hihelloy.work.omnibans.listener;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashMap;
import java.util.Map;

public final class PreLoginListener implements Listener {

    private final OmniBans plugin;

    public PreLoginListener(OmniBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String ip = event.getAddress().getHostAddress();
        Punishment ban = plugin.getStorage().findActiveBan(event.getUniqueId()).join();
        if (ban == null) {
            ban = plugin.getStorage().findActiveIpBan(ip).join();
        }
        if (ban == null || ban.isExpired()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", ban.getReason() != null ? ban.getReason() : "No reason specified");
        placeholders.put("staff", ban.getStaffName() != null ? ban.getStaffName() : "Console");
        placeholders.put("expires", ban.isPermanent() ? "Never" : TimeFormatter.formatRemaining(ban.getExpiresAt()));
        placeholders.put("id", String.valueOf(ban.getId()));
        String path = "IP_BAN".equals(ban.getType().name()) ? "banip.screen" : (ban.isPermanent() ? "ban.screen" : "tempban.screen");
        Component component = plugin.getMessages().component(path, placeholders);
        String legacyMessage = plugin.getMessageDispatcher().flatten(component);
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, legacyMessage);
        String displayName = ban.getTargetName() != null ? ban.getTargetName() : event.getName();
        plugin.getStaffAlertService().alertBanJoinAttempt(displayName);
    }

}