package com.hihelloy.work.omnibans.forge;

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
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.storage.sql.MySqlStorage;
import com.hihelloy.work.omnibans.common.storage.sql.SqliteStorage;
import com.hihelloy.work.omnibans.common.util.PluginLogger;
import com.hihelloy.work.omnibans.forge.command.ForgeCommands;
import com.hihelloy.work.omnibans.forge.config.ForgeModConfig;
import com.hihelloy.work.omnibans.forge.event.ForgeEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import com.hihelloy.work.omnibans.common.mod.ModBans;
net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Mod("omnibans")
public final class OmniBansForgeMod implements ModBans {

    private static OmniBansForgeMod instance;
    private static final Logger LOGGER = LogManager.getLogger("OmniBans");

    private ForgeModConfig config;
    private PunishmentStorage storage;
    private PunishmentCache cache;
    private SimpleEventBus eventBus;
    private CommonPunishmentManager punishmentManager;
    private CommonPlayerManager playerManager;
    private Executor asyncExecutor;
    private boolean spyAttempts = true;

    public OmniBansForgeMod(IEventBus modEventBus) {
        instance = this;
        modEventBus.addListener(this::onServerSetup);
    }

    public static OmniBansForgeMod getInstance() {
        return instance;
    }

    private void onServerSetup(FMLDedicatedServerSetupEvent event) {
        File configDir = Paths.get("config", "omnibans").toFile();
        configDir.mkdirs();
        PluginLogger pluginLogger = new ForgePluginLogger(LOGGER);
        ProxyMigrationService migration = new ProxyMigrationService(pluginLogger);
        config = new ForgeModConfig(configDir, pluginLogger);
        config.load();
        migration.migrate(new File(configDir, "config.properties"), config.openBundledStream(), List.of());
        config.load();
        asyncExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "OmniBans-Forge-Worker");
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
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new ForgeCommands(this));
        LOGGER.info("OmniBans Forge mod has been enabled.");
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
                return Platform.FORGE;
            }

            @Override
            public String getVersion() {
                return "1.0";
            }
        };
    }

    private PunishmentStorage buildStorage(File configDir, PluginLogger pluginLogger) {
        if (config.isMysql()) {
            return new MySqlStorage(config.getMysqlHost(), config.getMysqlPort(), config.getMysqlDatabase(), config.getMysqlUsername(), config.getMysqlPassword(), config.isMysqlUseSsl(), asyncExecutor, pluginLogger);
        }
        return new SqliteStorage(new File(configDir, "omnibans.db"), asyncExecutor, pluginLogger);
    }

    public ForgeModConfig getConfig() {
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

    public static Logger getModLogger() {
        return LOGGER;
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