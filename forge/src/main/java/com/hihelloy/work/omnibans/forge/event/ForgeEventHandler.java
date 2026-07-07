package com.hihelloy.work.omnibans.forge.event;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.forge.OmniBansForgeMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.net.InetSocketAddress;

public final class ForgeEventHandler {

    private final OmniBansForgeMod mod;

    public ForgeEventHandler(OmniBansForgeMod mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        String ip = resolveIp(player);
        mod.getStorage().recordSeenIp(player.getUUID(), player.getGameProfile().getName(), ip);
        mod.getStorage().findActiveBan(player.getUUID()).thenAccept(ban -> {
            if (ban == null) {
                mod.getStorage().findActiveIpBan(ip).thenAccept(ipBan -> {
                    if (ipBan != null && !ipBan.isExpired()) {
                        kickForBan(player, ipBan);
                    }
                });
                return;
            }
            if (!ban.isExpired()) {
                kickForBan(player, ban);
            }
        });
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        Punishment mute = mod.getCache().getMute(event.getPlayer().getUUID());
        if (mute == null || mute.isExpired()) {
            return;
        }
        event.setCanceled(true);
        String reason = mute.getReason() != null ? mute.getReason() : "No reason specified";
        event.getPlayer().sendSystemMessage(Component.literal("You are muted and cannot chat. Reason: " + reason));
    }

    private void kickForBan(ServerPlayer player, Punishment ban) {
        String reason = ban.getReason() != null ? ban.getReason() : "No reason specified";
        player.getServer().execute(() -> player.connection.disconnect(Component.literal(
            "You are banned from this server\nReason: " + reason)));
    }

    private String resolveIp(ServerPlayer player) {
        try {
            return ((InetSocketAddress) player.connection.getRemoteAddress()).getAddress().getHostAddress();
        } catch (Exception exception) {
            return "unknown";
        }
    }

}
