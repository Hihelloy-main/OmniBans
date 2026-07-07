package com.hihelloy.work.omnibans.sponge;

import com.google.inject.Inject;
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
import com.hihelloy.work.omnibans.common.mod.ModBans;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.storage.sql.MySqlStorage;
import com.hihelloy.work.omnibans.common.storage.sql.SqliteStorage;
import com.hihelloy.work.omnibans.common.util.PluginLogger;
import com.hihelloy.work.omnibans.sponge.config.SpongeModConfig;
import com.hihelloy.work.omnibans.sponge.listener.SpongeEventHandler;
import com.hihelloy.work.omnibans.sponge.command.SpongeCommands;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Plugin("omnibans")
public final class OmniBansSponge implements ModBans {

    private static OmniBansSponge instance;

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private PluginContainer container;

    private SpongeModConfig config;
    private PunishmentStorage storage;
    private PunishmentCache cache;
    private SimpleEventBus eventBus;
    private CommonPunishmentManager punishmentManager;
    private CommonPlayerManager playerManager;
    private Executor asyncExecutor;
    private boolean spyAttempts = true;

    public static OmniBansSponge getInstance() {
        return instance;
    }

    @Listener
    public void onStarted(StartedEngineEvent<Server> event) {
        instance = this;
        File configFolder = configDir.toFile();
        configFolder.mkdirs();
        PluginLogger pluginLogger = new SpongePluginLogger(logger);
        ProxyMigrationService migration = new ProxyMigrationService(pluginLogger);
        config = new SpongeModConfig(configFolder, pluginLogger);
        config.load();
        migration.migrate(new File(configFolder, "config.properties"), config.openBundledStream(), List.of());
        config.load();
        asyncExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "OmniBans-Sponge-Worker");
                thread.setDaemon(true);
                return thread;
            }
        });
        cache = new PunishmentCache();
        storage = buildStorage(configFolder, pluginLogger);
        storage.connect().join();
        eventBus = new SimpleEventBus();
        punishmentManager = new CommonPunishmentManager(storage, cache, eventBus, asyncExecutor, config.getServerName(), pluginLogger);
        playerManager = new CommonPlayerManager(storage);
        OmniBansProvider.register(buildApi());
        event.game().eventManager().registerListeners(container, new SpongeEventHandler(this));
        logger.info("OmniBans Sponge has been enabled.");
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent.Parameterized event) {
        SpongeCommands.register(event, this);
    }

    @Listener
    public void onStopping(StoppingEngineEvent<Server> event) {
        OmniBansProvider.unregister();
        if (storage != null) {
            storage.close();
        }
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

    private PunishmentStorage buildStorage(File configFolder, PluginLogger pluginLogger) {
        if (config.isMysql()) {
            return new MySqlStorage(config.getMysqlHost(), config.getMysqlPort(), config.getMysqlDatabase(), config.getMysqlUsername(), config.getMysqlPassword(), config.isMysqlUseSsl(), asyncExecutor, pluginLogger);
        }
        return new SqliteStorage(new File(configFolder, "omnibans.db"), asyncExecutor, pluginLogger);
    }

    @Override
    public PunishmentStorage getStorage() {
        return storage;
    }

    @Override
    public PunishmentCache getCache() {
        return cache;
    }

    @Override
    public CommonPunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    @Override
    public CommonPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public String getServerName() {
        return config.getServerName();
    }

    @Override
    public PluginLogger getLogger() {
        return new SpongePluginLogger(logger);
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

    public java.util.Optional<org.spongepowered.api.Server> getServer() {
        return org.spongepowered.api.Sponge.isServerAvailable() ? java.util.Optional.of(org.spongepowered.api.Sponge.server()) : java.util.Optional.empty();
    }

    public PluginContainer container() {
        return container;
    }

}
