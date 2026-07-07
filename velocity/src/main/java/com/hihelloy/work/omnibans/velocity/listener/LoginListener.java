package com.hihelloy.work.omnibans.velocity.listener;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import com.hihelloy.work.omnibans.velocity.OmniBansVelocity;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public final class LoginListener {

    private final OmniBansVelocity plugin;

    public LoginListener(OmniBansVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.EARLY)
    public EventTask onLogin(LoginEvent event) {
        return EventTask.async(() -> handle(event));
    }

    private void handle(LoginEvent event) {
        Player player = event.getPlayer();
        String ip = ((InetSocketAddress) player.getRemoteAddress()).getAddress().getHostAddress();
        Punishment ban = plugin.getStorage().findActiveBan(player.getUniqueId()).join();
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
        event.setResult(ResultedEvent.ComponentResult.denied(plugin.getMessages().component("ban.screen", placeholders)));
    }

}
