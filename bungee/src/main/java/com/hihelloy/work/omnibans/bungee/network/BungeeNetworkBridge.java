package com.hihelloy.work.omnibans.bungee.network;

import com.hihelloy.work.omnibans.bungee.OmniBansBungee;
import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeNetworkBridge {

    private final OmniBansBungee plugin;

    public BungeeNetworkBridge(OmniBansBungee plugin) {
        this.plugin = plugin;
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
        ProxiedPlayer player = plugin.getProxy().getPlayer(packet.getTargetUuid());
        if (player == null) {
            return;
        }
        player.disconnect(plugin.getMessages().components("ban.network-kick"));
    }

    private boolean isBanType(String type) {
        return "BAN".equals(type) || "IP_BAN".equals(type);
    }

}