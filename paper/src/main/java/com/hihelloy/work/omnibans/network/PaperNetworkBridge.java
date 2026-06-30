package com.hihelloy.work.omnibans.network;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class PaperNetworkBridge {

    private final OmniBans plugin;

    public PaperNetworkBridge(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getNetworkMessenger().subscribe(this::handle);
    }

    private void handle(NetworkPacket packet) {
        String origin = packet.getOriginServer();
        if (origin != null && origin.equals(plugin.getOmniBansConfig().getServerName())) {
            return;
        }
        plugin.getScheduler().runGlobal(() -> apply(packet));
    }

    private void apply(NetworkPacket packet) {
        if (packet.getTargetUuid() == null) {
            return;
        }
        if (packet.getAction() == NetworkAction.PUNISHMENT_REMOVED) {
            plugin.getCache().uncacheBan(packet.getTargetUuid());
            plugin.getCache().uncacheMute(packet.getTargetUuid());
            return;
        }
        plugin.getStorage().findActiveBan(packet.getTargetUuid()).thenAccept(ban -> {
            if (ban == null) {
                return;
            }
            plugin.getCache().cacheBan(ban);
            Player player = Bukkit.getPlayer(packet.getTargetUuid());
            if (player != null) {
                Component screen = buildBanScreen(ban);
                plugin.getScheduler().runGlobal(() -> plugin.getMessageDispatcher().kick(player, screen));
            }
        });
        plugin.getStorage().findActiveMute(packet.getTargetUuid()).thenAccept(mute -> {
            if (mute != null) {
                plugin.getCache().cacheMute(mute);
            }
        });
    }

    private Component buildBanScreen(Punishment ban) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", ban.getReason() != null ? ban.getReason() : "No reason specified");
        placeholders.put("staff", ban.getStaffName() != null ? ban.getStaffName() : "Console");
        placeholders.put("expires", ban.isPermanent() ? "Never" : TimeFormatter.formatRemaining(ban.getExpiresAt()));
        placeholders.put("id", String.valueOf(ban.getId()));
        return plugin.getMessages().component("ban.screen", placeholders);
    }

}
