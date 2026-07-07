package com.hihelloy.work.omnibans;

import com.hihelloy.work.omnibans.command.CommandOverrideService;
import com.hihelloy.work.omnibans.hook.DiscordSRVHook;
import com.hihelloy.work.omnibans.command.SubCommand;
import com.hihelloy.work.omnibans.command.executor.OmniBansExecutor;
import com.hihelloy.work.omnibans.command.impl.AltsCommand;
import com.hihelloy.work.omnibans.command.impl.BanCommand;
import com.hihelloy.work.omnibans.command.impl.BanIpCommand;
import com.hihelloy.work.omnibans.command.impl.BanListCommand;
import com.hihelloy.work.omnibans.command.impl.CheckCommand;
import com.hihelloy.work.omnibans.command.impl.HistoryCommand;
import com.hihelloy.work.omnibans.command.impl.KickCommand;
import com.hihelloy.work.omnibans.command.impl.MuteCommand;
import com.hihelloy.work.omnibans.command.impl.MuteListCommand;
import com.hihelloy.work.omnibans.command.impl.NoteCommand;
import com.hihelloy.work.omnibans.command.impl.OmniBansAdminCommand;
import com.hihelloy.work.omnibans.command.impl.TempBanCommand;
import com.hihelloy.work.omnibans.command.impl.TempMuteCommand;
import com.hihelloy.work.omnibans.command.impl.UnbanCommand;
import com.hihelloy.work.omnibans.command.impl.UnbanIpCommand;
import com.hihelloy.work.omnibans.command.impl.UnmuteCommand;
import com.hihelloy.work.omnibans.command.impl.WarnCommand;
import com.hihelloy.work.omnibans.common.cache.PunishmentCache;
import com.hihelloy.work.omnibans.common.network.NetworkMessenger;
import com.hihelloy.work.omnibans.common.network.NoopNetworkMessenger;
import com.hihelloy.work.omnibans.common.network.RedisNetworkMessenger;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.storage.sql.MySqlStorage;
import com.hihelloy.work.omnibans.common.storage.sql.SqliteStorage;
import com.hihelloy.work.omnibans.common.webhook.DiscordWebhook;
import com.hihelloy.work.omnibans.config.ConfigMigrationService;
import com.hihelloy.work.omnibans.config.MessagesConfig;
import com.hihelloy.work.omnibans.config.OmniBansConfig;
import com.hihelloy.work.omnibans.discord.DiscordAlertService;
import com.hihelloy.work.omnibans.gui.ConfigGuiListener;
import com.hihelloy.work.omnibans.gui.ConfigGuiService;
import com.hihelloy.work.omnibans.listener.ChatListener;
import com.hihelloy.work.omnibans.listener.CommandBlockListener;
import com.hihelloy.work.omnibans.listener.JoinListener;
import com.hihelloy.work.omnibans.listener.ModernChatListener;
import com.hihelloy.work.omnibans.listener.PreLoginListener;
import com.hihelloy.work.omnibans.network.PaperNetworkBridge;
import com.hihelloy.work.omnibans.scheduler.FoliaScheduler;
import com.hihelloy.work.omnibans.service.AltLookupService;
import com.hihelloy.work.omnibans.service.PlayerResolver;
import com.hihelloy.work.omnibans.service.PunishmentService;
import com.hihelloy.work.omnibans.service.StaffAlertService;
import com.hihelloy.work.omnibans.service.StaffExemptionService;
import com.hihelloy.work.omnibans.task.ExpiryTask;
import com.hihelloy.work.omnibans.task.SyncTask;
import com.hihelloy.work.omnibans.text.MessageDispatcher;
import com.hihelloy.work.omnibans.api.OmniBansProvider;
import com.hihelloy.work.omnibans.api.PaperOmniBansApi;
import com.hihelloy.work.omnibans.event.OmniBansBanEvent;
import com.hihelloy.work.omnibans.event.OmniBansKickEvent;
import com.hihelloy.work.omnibans.event.OmniBansMuteEvent;
import com.hihelloy.work.omnibans.event.OmniBansWarnEvent;
import com.hihelloy.work.omnibans.util.BannerPrinter;
import com.hihelloy.work.omnibans.util.PaperLoggerAdapter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class OmniBans extends JavaPlugin {

    private OmniBansConfig omniBansConfig;
    private MessagesConfig messagesConfig;
    private PunishmentStorage storage;
    private PunishmentCache cache;
    private NetworkMessenger networkMessenger;
    private DiscordWebhook discordWebhook;
    private FoliaScheduler scheduler;
    private PaperNetworkBridge networkBridge;
    private PlayerResolver playerResolver;
    private PunishmentService punishmentService;
    private PaperOmniBansApi omniBansApi;
    private AltLookupService altLookupService;
    private StaffExemptionService staffExemptionService;
    private StaffAlertService staffAlertService;
    private DiscordAlertService discordAlertService;
    private ConfigGuiService configGuiService;
    private MessageDispatcher messageDispatcher;
    private ExpiryTask expiryTask;
    private SyncTask syncTask;
    private Executor asyncExecutor;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    @Override
    public void onLoad() {
        scheduler = new FoliaScheduler(this);
    }

    @Override
    public void onEnable() {
        messageDispatcher = new MessageDispatcher();
        saveDefaultConfig();
        ConfigMigrationService configMigrationService = new ConfigMigrationService(this);
        configMigrationService.migrateConfig();
        omniBansConfig = new OmniBansConfig(this);
        omniBansConfig.load();
        configMigrationService.migrateMessages();
        messagesConfig = new MessagesConfig(this);
        messagesConfig.load();
        asyncExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "OmniBans-Worker");
                thread.setDaemon(true);
                return thread;
            }
        });
        cache = new PunishmentCache();
        storage = buildStorage();
        storage.connect().join();
        networkMessenger = buildNetworkMessenger();
        networkMessenger.connect();
        discordWebhook = new DiscordWebhook(omniBansConfig.getDiscordWebhookUrl(), new PaperLoggerAdapter(getLogger()));
        playerResolver = new PlayerResolver(this);
        punishmentService = new PunishmentService(this);
        altLookupService = new AltLookupService(this);
        staffExemptionService = new StaffExemptionService(this);
        staffAlertService = new StaffAlertService(this);
        discordAlertService = new DiscordAlertService(this);
        discordAlertService.reload();
        omniBansApi = new PaperOmniBansApi(this);
        omniBansApi.getSimpleEventBus().subscribe(com.hihelloy.work.omnibans.api.event.PostPunishmentEvent.class, event -> {
            if (!(event.getPunishment() instanceof com.hihelloy.work.omnibans.common.impl.CommonApiPunishment wrapper)) {
                return;
            }
            com.hihelloy.work.omnibans.common.punishment.Punishment p = wrapper.unwrap();
            switch (p.getType()) {
                case BAN:
                case IP_BAN:
                    getScheduler().runGlobal(() -> getServer().getPluginManager().callEvent(new OmniBansBanEvent(p)));
                    break;
                case MUTE:
                case IP_MUTE:
                    getScheduler().runGlobal(() -> getServer().getPluginManager().callEvent(new OmniBansMuteEvent(p)));
                    break;
                case KICK:
                    getScheduler().runGlobal(() -> getServer().getPluginManager().callEvent(new OmniBansKickEvent(p)));
                    break;
                case WARN:
                    getScheduler().runGlobal(() -> getServer().getPluginManager().callEvent(new OmniBansWarnEvent(p)));
                    break;
                default:
                    break;
            }
        });
        OmniBansProvider.register(omniBansApi);
        punishmentService.setPostPunishmentHandler(punishment -> omniBansApi.onPunishmentApplied(punishment));
        configGuiService = new ConfigGuiService(this);
        seedNameCaches();
        networkBridge = new PaperNetworkBridge(this);
        networkBridge.register();
        registerCommands();
        registerListeners();
        if (getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
            DiscordSRVHook.tryRegister(this);
        }
        expiryTask = new ExpiryTask(this);
        expiryTask.start();
        syncTask = new SyncTask(this);
        syncTask.start();
        new BannerPrinter(this).print();
    }

    @Override
    public void onDisable() {
        OmniBansProvider.unregister();
        if (expiryTask != null) {
            expiryTask.stop();
        }
        if (syncTask != null) {
            syncTask.stop();
        }
        if (networkMessenger != null) {
            networkMessenger.disconnect();
        }
        if (storage != null) {
            storage.close();
        }
        getLogger().info("OmniBans has been disabled.");
    }

    private void seedNameCaches() {
        storage.findAllKnownNames().thenAccept(names -> names.forEach(cache::addKnownName));
        storage.findActiveByType(PunishmentType.BAN).thenAccept(bans -> bans.forEach(cache::cacheBan));
        storage.findActiveByType(PunishmentType.MUTE).thenAccept(mutes -> mutes.forEach(cache::cacheMute));
    }

    private PunishmentStorage buildStorage() {
        if (omniBansConfig.getStorageType() == com.hihelloy.work.omnibans.common.storage.StorageType.MYSQL) {
            return new MySqlStorage(
                omniBansConfig.getMysqlHost(),
                omniBansConfig.getMysqlPort(),
                omniBansConfig.getMysqlDatabase(),
                omniBansConfig.getMysqlUsername(),
                omniBansConfig.getMysqlPassword(),
                omniBansConfig.isMysqlUseSsl(),
                asyncExecutor,
                new PaperLoggerAdapter(getLogger()));
        }
        File databaseFile = new File(getDataFolder(), "omnibans.db");
        return new SqliteStorage(databaseFile, asyncExecutor, new PaperLoggerAdapter(getLogger()));
    }

    private NetworkMessenger buildNetworkMessenger() {
        if (omniBansConfig.isRedisEnabled()) {
            return new RedisNetworkMessenger(
                omniBansConfig.getRedisHost(),
                omniBansConfig.getRedisPort(),
                omniBansConfig.getRedisPassword(),
                new PaperLoggerAdapter(getLogger()));
        }
        return new NoopNetworkMessenger();
    }

    private void registerCommands() {
        addSubCommand(new BanCommand(this));
        addSubCommand(new TempBanCommand(this));
        addSubCommand(new UnbanCommand(this));
        addSubCommand(new BanIpCommand(this));
        addSubCommand(new UnbanIpCommand(this));
        addSubCommand(new MuteCommand(this));
        addSubCommand(new TempMuteCommand(this));
        addSubCommand(new UnmuteCommand(this));
        addSubCommand(new KickCommand(this));
        addSubCommand(new WarnCommand(this));
        addSubCommand(new NoteCommand(this));
        addSubCommand(new HistoryCommand(this));
        addSubCommand(new CheckCommand(this));
        addSubCommand(new BanListCommand(this));
        addSubCommand(new MuteListCommand(this));
        addSubCommand(new AltsCommand(this));
        addSubCommand(new OmniBansAdminCommand(this));
        for (SubCommand subCommand : subCommands.values()) {
            bindCommand(subCommand.name(), subCommand);
        }
        bindCommand("ban-ip", subCommands.get("banip"));
        bindCommand("ipban", subCommands.get("banip"));
        bindCommand("ip-ban", subCommands.get("banip"));
        bindCommand("pardon", subCommands.get("unban"));
        bindCommand("pardon-ip", subCommands.get("unbanip"));
        bindCommand("silence", subCommands.get("mute"));
        bindCommand("tban", subCommands.get("tempban"));
        bindCommand("tmute", subCommands.get("tempmute"));
        new CommandOverrideService(this).applyIfNeeded();
    }

    private void bindCommand(String label, SubCommand subCommand) {
        PluginCommand pluginCommand = getCommand(label);
        if (pluginCommand == null) {
            getLogger().warning("Command not registered in plugin.yml: " + label);
            return;
        }
        OmniBansExecutor executor = new OmniBansExecutor(subCommand);
        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(executor);
    }

    private void addSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.name(), subCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PreLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ConfigGuiListener(this), this);
        if (isModernChatAvailable()) {
            getServer().getPluginManager().registerEvents(new ModernChatListener(this), this);
        } else {
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        }
    }

    private boolean isModernChatAvailable() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public OmniBansConfig getOmniBansConfig() {
        return omniBansConfig;
    }

    public MessagesConfig getMessages() {
        return messagesConfig;
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

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public FoliaScheduler getScheduler() {
        return scheduler;
    }

    public PaperNetworkBridge getNetworkBridge() {
        return networkBridge;
    }

    public PlayerResolver getPlayerResolver() {
        return playerResolver;
    }

    public PunishmentService getPunishmentService() {
        return punishmentService;
    }

    public AltLookupService getAltLookupService() {
        return altLookupService;
    }

    public StaffExemptionService getStaffExemptionService() {
        return staffExemptionService;
    }

    public StaffAlertService getStaffAlertService() {
        return staffAlertService;
    }

    public DiscordAlertService getDiscordAlertService() {
        return discordAlertService;
    }

    public ConfigGuiService getConfigGuiService() {
        return configGuiService;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

    public PaperLoggerAdapter getPaperLogger() {
        return new PaperLoggerAdapter(getLogger());
    }

    public PaperOmniBansApi getApi() {
        return omniBansApi;
    }

    public void reload() {
        omniBansConfig.load();
        messagesConfig.load();
        discordAlertService.reload();
    }

}
