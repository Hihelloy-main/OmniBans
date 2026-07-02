package com.hihelloy.work.omnibans.service;

import com.hihelloy.work.omnibans.OmniBans;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class StaffAlertService {

    private final OmniBans plugin;

    public StaffAlertService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void alertMutedChat(String playerName, String attemptedMessage) {
        if (!plugin.getOmniBansConfig().isSpyAttempts()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", playerName);
        placeholders.put("message", attemptedMessage);
        plugin.getScheduler().runGlobal(() -> broadcastToStaff("alerts.spy-muted-chat", placeholders));
    }

    public void alertBanJoinAttempt(String playerName) {
        if (!plugin.getOmniBansConfig().isSpyAttempts()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", playerName);
        plugin.getScheduler().runGlobal(() -> broadcastToStaff("alerts.ban-join-attempt", placeholders));
    }

    private void broadcastToStaff(String messagePath, Map<String, String> placeholders) {
        Component component = plugin.getMessages().component(messagePath, placeholders);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.isOp() || online.hasPermission("*")) {
                plugin.getMessageDispatcher().send(online, component);
            }
        }
        plugin.getMessageDispatcher().send(Bukkit.getConsoleSender(), component);
    }

}