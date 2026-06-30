package com.hihelloy.work.omnibans.listener;

import com.hihelloy.work.omnibans.OmniBans;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JoinListener implements Listener {

    private static final String ALERT_PERMISSION = "omnibans.alerts.alts";

    private final OmniBans plugin;

    public JoinListener(OmniBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getOmniBansConfig().isIpTracking()) {
            return;
        }
        Player player = event.getPlayer();
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return;
        }
        String ip = address.getAddress().getHostAddress();
        plugin.getCache().addKnownName(player.getName());
        plugin.getStorage().recordSeenIp(player.getUniqueId(), player.getName(), ip).thenRun(() -> checkForAlts(player));
    }

    private void checkForAlts(Player player) {
        if (plugin.getStaffExemptionService().isExempt(player)) {
            return;
        }
        plugin.getAltLookupService().findAlts(player.getUniqueId()).thenAccept(altMap -> {
            if (altMap.isEmpty()) {
                return;
            }
            plugin.getAltLookupService().findBannedAltNames(altMap).thenAccept(bannedAlts ->
                plugin.getScheduler().runGlobal(() -> {
                    alertStaff(player, altMap);
                    plugin.getDiscordAlertService().announceAltJoin(player.getName(), altMap, bannedAlts);
                }));
        });
    }

    private void alertStaff(Player player, Map<UUID, String> altMap) {
        if (!player.isOnline()) {
            return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", player.getName());
        placeholders.put("count", String.valueOf(altMap.size()));
        placeholders.put("alts", String.join(", ", altMap.values()));
        Component component = plugin.getMessages().component("alerts.alt-join", placeholders);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.isOp() || online.hasPermission(ALERT_PERMISSION)) {
                plugin.getMessageDispatcher().send(online, component);
            }
        }
        plugin.getMessageDispatcher().send(Bukkit.getConsoleSender(), component);
    }

}
