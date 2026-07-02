package com.hihelloy.work.omnibans.bungee.config;

import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class BungeeConfig {

    private final File dataFolder;
    private final PluginLogger logger;
    private final Properties properties = new Properties();

    public BungeeConfig(File dataFolder, PluginLogger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void load() {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File configFile = new File(dataFolder, "config.properties");
            if (!configFile.exists()) {
                writeDefaults(configFile);
            }
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            logger.severe("Failed to load OmniBans configuration: " + exception.getMessage());
        }
    }

    private void writeDefaults(File configFile) throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty("storage.type", "SQLITE");
        defaults.setProperty("storage.mysql.host", "localhost");
        defaults.setProperty("storage.mysql.port", "3306");
        defaults.setProperty("storage.mysql.database", "omnibans");
        defaults.setProperty("storage.mysql.username", "root");
        defaults.setProperty("storage.mysql.password", "");
        defaults.setProperty("storage.mysql.use-ssl", "false");
        defaults.setProperty("redis.enabled", "false");
        defaults.setProperty("redis.host", "localhost");
        defaults.setProperty("redis.port", "6379");
        defaults.setProperty("redis.password", "");
        defaults.setProperty("server-name", "proxy");
        defaults.setProperty("prefix", "&8[&cOmniBans&8]");
        try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
            defaults.store(outputStream, "OmniBans proxy configuration. storage.type SQLITE needs nothing installed and just creates a file next to this one, switch to MYSQL only if every server and this proxy should share one database");
        }
    }

    public boolean isMysql() {
        return "MYSQL".equalsIgnoreCase(properties.getProperty("storage.type", "SQLITE"));
    }

    public String getMysqlHost() {
        return properties.getProperty("storage.mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return Integer.parseInt(properties.getProperty("storage.mysql.port", "3306"));
    }

    public String getMysqlDatabase() {
        return properties.getProperty("storage.mysql.database", "omnibans");
    }

    public String getMysqlUsername() {
        return properties.getProperty("storage.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return properties.getProperty("storage.mysql.password", "");
    }

    public boolean isMysqlUseSsl() {
        return Boolean.parseBoolean(properties.getProperty("storage.mysql.use-ssl", "false"));
    }

    public boolean isRedisEnabled() {
        return Boolean.parseBoolean(properties.getProperty("redis.enabled", "false"));
    }

    public String getRedisHost() {
        return properties.getProperty("redis.host", "localhost");
    }

    public int getRedisPort() {
        return Integer.parseInt(properties.getProperty("redis.port", "6379"));
    }

    public String getRedisPassword() {
        return properties.getProperty("redis.password", "");
    }

    public String getServerName() {
        return properties.getProperty("server-name", "proxy");
    }

    public String getPrefix() {
        return properties.getProperty("prefix", "&8[&cOmniBans&8]");
    }

    public java.io.InputStream openBundledStream() {
        Properties defaults = new Properties();
        defaults.setProperty("storage.type", "SQLITE");
        defaults.setProperty("storage.mysql.host", "localhost");
        defaults.setProperty("storage.mysql.port", "3306");
        defaults.setProperty("storage.mysql.database", "omnibans");
        defaults.setProperty("storage.mysql.username", "root");
        defaults.setProperty("storage.mysql.password", "");
        defaults.setProperty("storage.mysql.use-ssl", "false");
        defaults.setProperty("redis.enabled", "false");
        defaults.setProperty("redis.host", "localhost");
        defaults.setProperty("redis.port", "6379");
        defaults.setProperty("redis.password", "");
        defaults.setProperty("server-name", "proxy");
        defaults.setProperty("prefix", "&8[&cOmniBans&8]");
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        try {
            defaults.store(buffer, null);
        } catch (IOException exception) {
            return null;
        }
        return new java.io.ByteArrayInputStream(buffer.toByteArray());
    }

}