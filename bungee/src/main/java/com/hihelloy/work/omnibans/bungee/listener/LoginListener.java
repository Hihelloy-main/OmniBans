package com.hihelloy.work.omnibans.bungee.listener;

import com.hihelloy.work.omnibans.bungee.OmniBansBungee;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;

public final class LoginListener implements Listener {

    private final OmniBansBungee plugin;

    public LoginListener(OmniBansBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        event.registerIntent(plugin);
        plugin.getProxy().getScheduler().runAsync(plugin, () -> handle(event));
    }

    private void handle(LoginEvent event) {
        try {
            checkBan(event);
        } finally {
            event.completeIntent(plugin);
        }
    }

    private void checkBan(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        String ip = ((InetSocketAddress) connection.getSocketAddress()).getAddress().getHostAddress();
        Punishment ban = plugin.getStorage().findActiveBan(connection.getUniqueId()).join();
        if (ban == null) {
            ban = plugin.getStorage().findActiveIpBan(ip).join();
        }
        if (ban == null || ban.isExpired()) {
            return;
        }
        String reason = ban.getReason() != null ? ban.getReason() : "No reason specified";
        String staff = ban.getStaffName() != null ? ban.getStaffName() : "Console";
        String expires = ban.isPermanent() ? "Never" : TimeFormatter.formatRemaining(ban.getExpiresAt());
        String message = ChatColor.translateAlternateColorCodes('&', "&c&lYou are banned from this network\n&7Reason: &f" + reason + "\n&7Expires: &f" + expires + "\n&7Staff: &f" + staff);
        event.setCancelled(true);
        event.setCancelReason(TextComponent.fromLegacyText(message));
    }

}
