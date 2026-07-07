package com.hihelloy.work.omnibans.fabric.event;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.fabric.OmniBansFabricMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.net.InetSocketAddress;
import java.util.Map;

public final class FabricEventHandler {

    private FabricEventHandler() {
    }

    public static void register(OmniBansFabricMod mod) {
        registerLoginCheck(mod);
        registerChatCheck(mod);
        registerJoinRecording(mod);
    }

    private static void registerLoginCheck(OmniBansFabricMod mod) {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            String ip = resolveIp(handler);
            Punishment ban = mod.getStorage().findActiveBan(handler.player.getUUID()).join();
            if (ban == null) {
                ban = mod.getStorage().findActiveIpBan(ip).join();
            }
            if (ban == null || ban.isExpired()) {
                return;
            }
            String reason = ban.getReason() != null ? ban.getReason() : "No reason specified";
            handler.disconnect(Component.literal(
                "You are banned from this server\nReason: " + reason +
                "\nExpires: " + (ban.isPermanent() ? "Never" : formatRemaining(ban.getExpiresAt()))));
        });
    }

    private static void registerChatCheck(OmniBansFabricMod mod) {
        ServerMessageEvents.ALLOW_CHAT.register((message, sender, params) -> {
            Punishment mute = mod.getCache().getMute(sender.getUUID());
            if (mute == null || mute.isExpired()) {
                return true;
            }
            String reason = mute.getReason() != null ? mute.getReason() : "No reason specified";
            sender.sendSystemMessage(Component.literal(
                "You are muted and cannot chat. Reason: " + reason));
            return false;
        });
    }

    private static void registerJoinRecording(OmniBansFabricMod mod) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            String ip = resolveIp(handler);
            mod.getStorage().recordSeenIp(
                handler.player.getUUID(),
                handler.player.getGameProfile().getName(),
                ip);
        });
    }

    private static String resolveIp(ServerGamePacketListenerImpl handler) {
        try {
            return ((InetSocketAddress) handler.getRemoteAddress()).getAddress().getHostAddress();
        } catch (Exception exception) {
            return "unknown";
        }
    }

    private static String formatRemaining(long expiresAt) {
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) {
            return "Expired";
        }
        long seconds = remaining / 1000L;
        long days = seconds / 86400L;
        seconds %= 86400L;
        long hours = seconds / 3600L;
        seconds %= 3600L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || builder.length() == 0) {
            builder.append(seconds).append("s");
        }
        return builder.toString().trim();
    }

}
