package com.hihelloy.work.omnibans.config;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.storage.StorageType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public final class OmniBansConfig {

    private final OmniBans plugin;
    private String prefix;
    private String serverName;
    private StorageType storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private boolean mysqlUseSsl;
    private boolean redisEnabled;
    private String redisHost;
    private int redisPort;
    private String redisPassword;
    private boolean discordEnabled;
    private String discordWebhookUrl;
    private boolean discordBotEnabled;
    private String discordBotToken;
    private String discordServerId;
    private String discordChannelId;
    private String discordAdminRoleId;
    private int warnThreshold;
    private String warnPunishment;
    private String warnPunishmentDuration;
    private List<String> blockedCommandsWhileMuted;
    private List<String> commonReasons;
    private boolean broadcastBan;
    private boolean broadcastMute;
    private boolean broadcastKick;
    private boolean broadcastWarn;
    private boolean ipTracking;
    private boolean spyAttempts;

    public OmniBansConfig(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        prefix = config.getString("prefix", "<gray>[<red>OmniBans</red><gray>]");
        serverName = config.getString("server-name", "default");
        storageType = StorageType.valueOf(config.getString("storage.type", "SQLITE").toUpperCase());
        mysqlHost = config.getString("storage.mysql.host", "localhost");
        mysqlPort = config.getInt("storage.mysql.port", 3306);
        mysqlDatabase = config.getString("storage.mysql.database", "omnibans");
        mysqlUsername = config.getString("storage.mysql.username", "root");
        mysqlPassword = config.getString("storage.mysql.password", "");
        mysqlUseSsl = config.getBoolean("storage.mysql.use-ssl", false);
        redisEnabled = config.getBoolean("redis.enabled", false);
        redisHost = config.getString("redis.host", "localhost");
        redisPort = config.getInt("redis.port", 6379);
        redisPassword = config.getString("redis.password", "");
        discordEnabled = config.getBoolean("discord.enabled", false);
        discordWebhookUrl = config.getString("discord.webhook-url", "");
        discordBotEnabled = config.getBoolean("discord-bot.enabled", false);
        discordBotToken = config.getString("discord-bot.token", "");
        discordServerId = config.getString("discord-bot.server-id", "");
        discordChannelId = config.getString("discord-bot.channel-id", "");
        discordAdminRoleId = config.getString("discord-bot.admin-role-id", "");
        warnThreshold = config.getInt("punishments.warn-threshold", 3);
        warnPunishment = config.getString("punishments.warn-punishment", "tempban");
        warnPunishmentDuration = config.getString("punishments.warn-punishment-duration", "1d");
        blockedCommandsWhileMuted = config.getStringList("punishments.blocked-commands-while-muted");
        commonReasons = config.getStringList("punishments.common-reasons");
        broadcastBan = config.getBoolean("broadcast.ban", true);
        broadcastMute = config.getBoolean("broadcast.mute", true);
        broadcastKick = config.getBoolean("broadcast.kick", false);
        broadcastWarn = config.getBoolean("broadcast.warn", true);
        ipTracking = config.getBoolean("ip-tracking", true);
        spyAttempts = config.getBoolean("alerts.spy-attempts", true);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getServerName() {
        return serverName;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public boolean isMysqlUseSsl() {
        return mysqlUseSsl;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public boolean isDiscordEnabled() {
        return discordEnabled;
    }

    public String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public boolean isDiscordBotEnabled() {
        return discordBotEnabled;
    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getDiscordServerId() {
        return discordServerId;
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public String getDiscordAdminRoleId() {
        return discordAdminRoleId;
    }

    public int getWarnThreshold() {
        return warnThreshold;
    }

    public String getWarnPunishment() {
        return warnPunishment;
    }

    public String getWarnPunishmentDuration() {
        return warnPunishmentDuration;
    }

    public List<String> getBlockedCommandsWhileMuted() {
        return blockedCommandsWhileMuted;
    }

    public List<String> getCommonReasons() {
        return commonReasons;
    }

    public boolean isBroadcastBan() {
        return broadcastBan;
    }

    public boolean isBroadcastMute() {
        return broadcastMute;
    }

    public boolean isBroadcastKick() {
        return broadcastKick;
    }

    public boolean isBroadcastWarn() {
        return broadcastWarn;
    }

    public boolean isIpTracking() {
        return ipTracking;
    }

    public boolean isSpyAttempts() {
        return spyAttempts;
    }

}