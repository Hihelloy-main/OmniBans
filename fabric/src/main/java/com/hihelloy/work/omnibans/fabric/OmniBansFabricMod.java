package com.hihelloy.work.omnibans.fabric;

import com.hihelloy.work.omnibans.api.OmniBansApi;
import com.hihelloy.work.omnibans.api.OmniBansProvider;
import com.hihelloy.work.omnibans.api.event.OmniBansEventBus;
import com.hihelloy.work.omnibans.api.manager.PlayerManager;
import com.hihelloy.work.omnibans.api.manager.PunishmentManager;
import com.hihelloy.work.omnibans.api.platform.Platform;
import com.hihelloy.work.omnibans.common.cache.PunishmentCache;
import com.hihelloy.work.omnibans.common.config.ProxyMigrationService;
import com.hihelloy.work.omnibans.common.impl.CommonPlayerManager;
import com.hihelloy.work.omnibans.common.impl.CommonPunishmentManager;
import com.hihelloy.work.omnibans.common.impl.SimpleEventBus;
import com.hihelloy.work.omnibans.common.network.NoopNetworkMessenger;
import com.hihelloy.work.omnibans.common.network.RedisNetworkMessenger;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.storage.sql.MySqlStorage;
import com.hihelloy.work.omnibans.common.storage.sql.SqliteStorage;
import com.hihelloy.work.omnibans.common.util.PluginLogger;
import com.hihelloy.work.omnibans.fabric.command.FabricCommands;
import com.hihelloy.work.omnibans.fabric.config.FabricModConfig;
import com.hihelloy.work.omnibans.fabric.event.FabricEventHandler;
import com.hihelloy.work.omnibans.common.mod.ModBans;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class OmniBansFabricMod implements DedicatedServerModInitializer, ModBans {

    private static OmniBansFabricMod instance;

    private final Logger logger = LoggerFactory.getLogger("OmniBans");
    private FabricModConfig config;
    private PunishmentStorage storage;
    private PunishmentCache cache;
    private SimpleEventBus eventBus;
    private CommonPunishmentManager punishmentManager;
    private CommonPlayerManager playerManager;
    private Executor asyncExecutor;
    private boolean spyAttempts = true;

    public static OmniBansFabricMod getInstance() {
        return instance;
    }

    @Override
    public void onInitializeServer() {
        instance = this;
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("omnibans");
        configDir.toFile().mkdirs();
        PluginLogger pluginLogger = new FabricPluginLogger(logger);
        ProxyMigrationService migration = new ProxyMigrationService(pluginLogger);
        config = new FabricModConfig(configDir.toFile(), pluginLogger);
        config.load();
        migration.migrate(configDir.resolve("config.properties").toFile(), config.openBundledStream(), List.of());
        config.load();
        asyncExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "OmniBans-Fabric-Worker");
                thread.setDaemon(true);
                return thread;
            }
        });
        cache = new PunishmentCache();
        storage = buildStorage(configDir, pluginLogger);
        storage.connect().join();
        eventBus = new SimpleEventBus();
        punishmentManager = new CommonPunishmentManager(storage, cache, eventBus, asyncExecutor, config.getServerName(), pluginLogger);
        playerManager = new CommonPlayerManager(storage);
        OmniBansProvider.register(buildApi());
        FabricEventHandler.register(this);
        FabricCommands.register(this);
        logger.info("OmniBans Fabric mod has been enabled.");
    }

    private OmniBansApi buildApi() {
        return new OmniBansApi() {
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
                return Platform.FABRIC;
            }

            @Override
            public String getVersion() {
                return "1.0";
            }
        };
    }

    private PunishmentStorage buildStorage(Path configDir, PluginLogger pluginLogger) {
        if (config.isMysql()) {
            return new MySqlStorage(config.getMysqlHost(), config.getMysqlPort(), config.getMysqlDatabase(), config.getMysqlUsername(), config.getMysqlPassword(), config.isMysqlUseSsl(), asyncExecutor, pluginLogger);
        }
        File databaseFile = configDir.resolve("omnibans.db").toFile();
        return new SqliteStorage(databaseFile, asyncExecutor, pluginLogger);
    }

    public FabricModConfig getConfig() {
        return config;
    }

    public PunishmentStorage getStorage() {
        return storage;
    }

    public PunishmentCache getCache() {
        return cache;
    }

    public CommonPunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getServerName() {
        return config.getServerName();
    }

    @Override
    public boolean isSpyAttempts() {
        return spyAttempts;
    }

    @Override
    public void setSpyAttempts(boolean value) {
        spyAttempts = value;
    }

    @Override
    public void reload() {
        config.load();
    }

}
