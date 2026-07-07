package com.hihelloy.work.omnibans.bungee;

import com.hihelloy.work.omnibans.bungee.command.NetBanCommand;
import com.hihelloy.work.omnibans.bungee.command.NetMuteCommand;
import com.hihelloy.work.omnibans.bungee.config.BungeeConfig;
import com.hihelloy.work.omnibans.bungee.config.BungeeMessagesConfig;
import com.hihelloy.work.omnibans.bungee.listener.LoginListener;
import com.hihelloy.work.omnibans.bungee.network.BungeeNetworkBridge;
import com.hihelloy.work.omnibans.bungee.util.BungeeLoggerAdapter;
import com.hihelloy.work.omnibans.common.cache.PunishmentCache;
import com.hihelloy.work.omnibans.common.config.ProxyMigrationService;
import com.hihelloy.work.omnibans.common.network.NetworkMessenger;
import com.hihelloy.work.omnibans.common.network.NoopNetworkMessenger;
import com.hihelloy.work.omnibans.common.network.RedisNetworkMessenger;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.storage.sql.MySqlStorage;
import com.hihelloy.work.omnibans.common.storage.sql.SqliteStorage;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class OmniBansBungee extends Plugin {

    private BungeeConfig config;
    private BungeeMessagesConfig messages;
    private PunishmentStorage storage;
    private PunishmentCache cache;
    private NetworkMessenger networkMessenger;
    private BungeeNetworkBridge networkBridge;
    private Executor asyncExecutor;
    private BungeeLoggerAdapter loggerAdapter;

    @Override
    public void onEnable() {
        loggerAdapter = new BungeeLoggerAdapter(getLogger());
        ProxyMigrationService migration = new ProxyMigrationService(loggerAdapter);
        config = new BungeeConfig(getDataFolder(), loggerAdapter);
        config.load();
        migration.migrate(new File(getDataFolder(), "config.properties"), config.openBundledStream(), List.of());
        config.load();
        messages = new BungeeMessagesConfig(config, getDataFolder(), getLogger());
        messages.load();
        migration.migrate(new File(getDataFolder(), "messages.properties"), messages.openBundledStream(), List.of());
        messages.load();
        asyncExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "OmniBans-Bungee-Worker");
                thread.setDaemon(true);
                return thread;
            }
        });
        cache = new PunishmentCache();
        storage = buildStorage();
        storage.connect().join();
        networkMessenger = buildNetworkMessenger();
        networkMessenger.connect();
        networkBridge = new BungeeNetworkBridge(this);
        networkBridge.register();
        getProxy().getPluginManager().registerListener(this, new LoginListener(this));
        getProxy().getPluginManager().registerCommand(this, new NetBanCommand(this));
        getProxy().getPluginManager().registerCommand(this, new NetMuteCommand(this));
        getLogger().info("OmniBans Bungee bridge has been enabled.");
    }

    @Override
    public void onDisable() {
        if (networkMessenger != null) {
            networkMessenger.disconnect();
        }
        if (storage != null) {
            storage.close();
        }
    }

    private PunishmentStorage buildStorage() {
        if (config.isMysql()) {
            return new MySqlStorage(
                config.getMysqlHost(),
                config.getMysqlPort(),
                config.getMysqlDatabase(),
                config.getMysqlUsername(),
                config.getMysqlPassword(),
                config.isMysqlUseSsl(),
                asyncExecutor,
                loggerAdapter);
        }
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File databaseFile = new File(getDataFolder(), "omnibans.db");
        return new SqliteStorage(databaseFile, asyncExecutor, loggerAdapter);
    }

    private NetworkMessenger buildNetworkMessenger() {
        if (config.isRedisEnabled()) {
            return new RedisNetworkMessenger(
                config.getRedisHost(),
                config.getRedisPort(),
                config.getRedisPassword(),
                loggerAdapter);
        }
        return new NoopNetworkMessenger();
    }

    public BungeeConfig getBungeeConfig() {
        return config;
    }

    public BungeeMessagesConfig getMessages() {
        return messages;
    }

    public PunishmentStorage getStorage() {
        return storage;
    }

    public PunishmentCache getCache() {
        return cache;
    }

    public NetworkMessenger getNetworkMessenger() {
        return networkMessenger;
    }

}
