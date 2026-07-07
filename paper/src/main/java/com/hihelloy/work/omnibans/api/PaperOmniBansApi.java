package com.hihelloy.work.omnibans.api;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.api.event.OmniBansEventBus;
import com.hihelloy.work.omnibans.api.manager.PlayerManager;
import com.hihelloy.work.omnibans.api.manager.PunishmentManager;
import com.hihelloy.work.omnibans.api.platform.Platform;
import com.hihelloy.work.omnibans.common.impl.CommonPlayerManager;
import com.hihelloy.work.omnibans.common.impl.CommonPunishmentManager;
import com.hihelloy.work.omnibans.common.impl.SimpleEventBus;

public final class PaperOmniBansApi implements OmniBansApi {

    private final OmniBans plugin;
    private final SimpleEventBus eventBus;
    private final CommonPunishmentManager punishmentManager;
    private final CommonPlayerManager playerManager;

    public PaperOmniBansApi(OmniBans plugin) {
        this.plugin = plugin;
        this.eventBus = new SimpleEventBus();
        this.punishmentManager = new CommonPunishmentManager(
            plugin.getStorage(),
            plugin.getCache(),
            eventBus,
            plugin.getAsyncExecutor(),
            plugin.getOmniBansConfig().getServerName(),
            plugin.getPaperLogger());
        this.playerManager = new CommonPlayerManager(plugin.getStorage());
    }

    public SimpleEventBus getSimpleEventBus() {
        return eventBus;
    }

    public void onPunishmentApplied(com.hihelloy.work.omnibans.common.punishment.Punishment punishment) {
        eventBus.post(new PostPunishmentEventImpl(new com.hihelloy.work.omnibans.common.impl.CommonApiPunishment(punishment)));
    }

    @Override
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public OmniBansEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public Platform getPlatform() {
        return plugin.getScheduler().isFolia() ? Platform.FOLIA : Platform.PAPER;
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

}
