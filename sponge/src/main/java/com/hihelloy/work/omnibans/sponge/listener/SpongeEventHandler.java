package com.hihelloy.work.omnibans.sponge.listener;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.sponge.OmniBansSponge;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.net.InetSocketAddress;

public final class SpongeEventHandler {

    private final OmniBansSponge plugin;

    public SpongeEventHandler(OmniBansSponge plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onLogin(ServerSideConnectionEvent.Auth event) {
        String ip = ((InetSocketAddress) event.connection().address()).getAddress().getHostAddress();
        event.connection().profile().uuid().ifPresent(uuid -> {
            Punishment ban = plugin.getStorage().findActiveBan(uuid).join();
            if (ban == null) {
                ban = plugin.getStorage().findActiveIpBan(ip).join();
            }
            if (ban != null && !ban.isExpired()) {
                String reason = ban.getReason() != null ? ban.getReason() : "No reason specified";
                event.setCancelled(true);
                event.setMessage(Component.text("You are banned from this server.\nReason: " + reason));
            }
        });
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        ServerPlayer player = event.player();
        String ip = ((InetSocketAddress) player.connection().address()).getAddress().getHostAddress();
        plugin.getStorage().recordSeenIp(player.uniqueId(), player.name(), ip);
    }

    @Listener
    public void onChat(PlayerChatEvent event, @First ServerPlayer player) {
        Punishment mute = plugin.getCache().getMute(player.uniqueId());
        if (mute == null || mute.isExpired()) {
            return;
        }
        event.setCancelled(true);
        String reason = mute.getReason() != null ? mute.getReason() : "No reason specified";
        player.sendMessage(Component.text("You are muted. Reason: " + reason));
    }

}
