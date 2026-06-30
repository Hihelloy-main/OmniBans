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
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.net.InetSocketAddress;

public final class LoginListener {

    private final OmniBansVelocity plugin;
    private final MiniMessage miniMessage;

    public LoginListener(OmniBansVelocity plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
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
        String reason = ban.getReason() != null ? ban.getReason() : "No reason specified";
        String staff = ban.getStaffName() != null ? ban.getStaffName() : "Console";
        String expires = ban.isPermanent() ? "Never" : TimeFormatter.formatRemaining(ban.getExpiresAt());
        String message = "<red><bold>You are banned from this network\n<gray>Reason: <white>" + reason + "\n<gray>Expires: <white>" + expires + "\n<gray>Staff: <white>" + staff;
        event.setResult(ResultedEvent.ComponentResult.denied(miniMessage.deserialize(message)));
    }

}
