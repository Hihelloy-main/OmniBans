package com.hihelloy.work.omnibans.gui;

import com.hihelloy.work.omnibans.OmniBans;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

public final class ConfigGuiListener implements Listener {

    private final OmniBans plugin;

    public ConfigGuiListener(OmniBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof ConfigGuiHolder configGuiHolder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
            return;
        }
        plugin.getConfigGuiService().handleClick(player, configGuiHolder, event.getSlot());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getConfigGuiService().clearPending(event.getPlayer());
    }

}