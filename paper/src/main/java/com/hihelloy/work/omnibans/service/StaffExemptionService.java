package com.hihelloy.work.omnibans.service;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.hook.LuckPermsBridge;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StaffExemptionService {

    private final OmniBans plugin;
    private final boolean luckPermsPresent;

    public StaffExemptionService(OmniBans plugin) {
        this.plugin = plugin;
        this.luckPermsPresent = checkLuckPerms();
    }

    private boolean checkLuckPerms() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public boolean isExempt(Player player) {
        if (player.isOp()) {
            return true;
        }
        return player.hasPermission("*");
    }

    public CompletableFuture<Boolean> isExempt(UUID uuid) {
        return resolvePermission(uuid, "*", true);
    }

    public CompletableFuture<Boolean> hasPunishmentExemption(UUID uuid, String node) {
        return resolvePermission(uuid, node, false);
    }

    private CompletableFuture<Boolean> resolvePermission(UUID uuid, String node, boolean treatOpAsExempt) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            if (treatOpAsExempt && online.isOp()) {
                return CompletableFuture.completedFuture(true);
            }
            return CompletableFuture.completedFuture(online.hasPermission(node));
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (treatOpAsExempt && offlinePlayer.isOp()) {
            return CompletableFuture.completedFuture(true);
        }
        if (!luckPermsPresent) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(() -> queryLuckPerms(uuid, node), plugin.getAsyncExecutor());
    }

    private boolean queryLuckPerms(UUID uuid, String node) {
        try {
            return LuckPermsBridge.hasPermission(uuid, node);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to query LuckPerms for " + uuid + ": " + exception.getMessage());
            return false;
        }
    }

}
