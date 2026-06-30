package com.hihelloy.work.omnibans.velocity;

import com.google.inject.Inject;
import com.hihelloy.work.omnibans.common.cache.PunishmentCache;
import com.hihelloy.work.omnibans.common.network.NetworkMessenger;
import com.hihelloy.work.omnibans.common.network.NoopNetworkMessenger;
import com.hihelloy.work.omnibans.common.network.RedisNetworkMessenger;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.storage.sql.MySqlStorage;
import com.hihelloy.work.omnibans.common.storage.sql.SqliteStorage;
import com.hihelloy.work.omnibans.velocity.command.NetBanCommand;
import com.hihelloy.work.omnibans.velocity.command.NetMuteCommand;
import com.hihelloy.work.omnibans.velocity.config.VelocityConfig;
import com.hihelloy.work.omnibans.velocity.listener.LoginListener;
import com.hihelloy.work.omnibans.velocity.network.VelocityNetworkBridge;
import com.hihelloy.work.omnibans.velocity.util.VelocityLoggerAdapter;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Plugin(id = "omnibans", name = "OmniBans", version = "1.0", authors = {"Hihelloy"})
public final class OmniBansVelocity {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private VelocityConfig config;
    private PunishmentStorage storage;
    private PunishmentCache cache;
    private NetworkMessenger networkMessenger;
    private VelocityNetworkBridge networkBridge;
    private Executor asyncExecutor;

    @Inject
    public OmniBansVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        config = new VelocityConfig(dataDirectory, logger);
        config.load();
        asyncExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "OmniBans-Velocity-Worker");
                thread.setDaemon(true);
                return thread;
            }
        });
        cache = new PunishmentCache();
        storage = buildStorage();
        storage.connect().join();
        networkMessenger = buildNetworkMessenger();
        networkMessenger.connect();
        networkBridge = new VelocityNetworkBridge(this);
        networkBridge.register();
        proxyServer.getEventManager().register(this, new LoginListener(this));
        proxyServer.getCommandManager().register(proxyServer.getCommandManager().metaBuilder("netban").build(), new NetBanCommand(this));
        proxyServer.getCommandManager().register(proxyServer.getCommandManager().metaBuilder("netmute").build(), new NetMuteCommand(this));
        logger.info("OmniBans Velocity bridge has been enabled.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
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
                    new VelocityLoggerAdapter(logger));
        }
        File databaseFile = dataDirectory.resolve("omnibans.db").toFile();
        File parentFolder = databaseFile.getParentFile();
        if (parentFolder != null) {
            parentFolder.mkdirs();
        }
        return new SqliteStorage(databaseFile, asyncExecutor, new VelocityLoggerAdapter(logger));
    }

    private NetworkMessenger buildNetworkMessenger() {
        if (config.isRedisEnabled()) {
            return new RedisNetworkMessenger(
                    config.getRedisHost(),
                    config.getRedisPort(),
                    config.getRedisPassword(),
                    new VelocityLoggerAdapter(logger));
        }
        return new NoopNetworkMessenger();
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Logger getLogger() {
        return logger;
    }

    public VelocityConfig getVelocityConfig() {
        return config;
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

    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

}