package com.hihelloy.work.omnibans.hook;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;

public final class DiscordSRVHook {

    private final OmniBans plugin;

    private DiscordSRVHook(OmniBans plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onGameChatPreProcess(GameChatMessagePreProcessEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        Punishment mute = plugin.getCache().getMute(event.getPlayer().getUniqueId());
        if (mute == null || mute.isExpired()) {
            return;
        }
        event.setCancelled(true);
    }

    public static void tryRegister(OmniBans plugin) {
        try {
            DiscordSRV.api.subscribe(new DiscordSRVHook(plugin));
            plugin.getLogger().info("Hooked into DiscordSRV, muted player messages will not be forwarded to Discord.");
        } catch (Exception exception) {
            plugin.getLogger().warning("DiscordSRV detected but hook registration failed: " + exception.getMessage());
        }
    }

}
