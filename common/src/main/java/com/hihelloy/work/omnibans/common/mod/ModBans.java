package com.hihelloy.work.omnibans.common.mod;

import com.hihelloy.work.omnibans.common.cache.PunishmentCache;
import com.hihelloy.work.omnibans.common.impl.CommonPlayerManager;
import com.hihelloy.work.omnibans.common.impl.CommonPunishmentManager;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.util.PluginLogger;

public interface ModBans {

    PunishmentStorage getStorage();

    PunishmentCache getCache();

    CommonPunishmentManager getPunishmentManager();

    CommonPlayerManager getPlayerManager();

    String getServerName();

    PluginLogger getLogger();

    boolean isSpyAttempts();

    void setSpyAttempts(boolean value);

    void reload();

}
