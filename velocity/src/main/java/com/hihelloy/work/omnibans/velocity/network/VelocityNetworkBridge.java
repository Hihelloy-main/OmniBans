package com.hihelloy.work.omnibans.velocity.network;

import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.velocity.OmniBansVelocity;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class VelocityNetworkBridge {

    private final OmniBansVelocity plugin;
    private final MiniMessage miniMessage;

    public VelocityNetworkBridge(OmniBansVelocity plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void register() {
        plugin.getNetworkMessenger().subscribe(this::handle);
    }

    private void handle(NetworkPacket packet) {
        if (packet.getAction() != NetworkAction.PUNISHMENT_ADDED) {
            return;
        }
        if (packet.getTargetUuid() == null) {
            return;
        }
        if (!isBanType(packet.getPunishmentType())) {
            return;
        }
        Player player = plugin.getProxyServer().getPlayer(packet.getTargetUuid()).orElse(null);
        if (player == null) {
            return;
        }
        Component message = miniMessage.deserialize("<red><bold>You have been banned from this network");
        player.disconnect(message);
    }

    private boolean isBanType(String type) {
        return "BAN".equals(type) || "IP_BAN".equals(type);
    }

}
