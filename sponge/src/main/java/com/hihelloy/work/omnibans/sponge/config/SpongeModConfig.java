package com.hihelloy.work.omnibans.sponge.config;

import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class SpongeModConfig {

    private final File configDir;
    private final PluginLogger logger;
    private final Properties properties = new Properties();

    public SpongeModConfig(File configDir, PluginLogger logger) {
        this.configDir = configDir;
        this.logger = logger;
    }

    public void load() {
        File file = new File(configDir, "config.properties");
        if (!file.exists()) {
            writeDefaults(file);
        }
        try (FileInputStream stream = new FileInputStream(file)) {
            properties.load(stream);
        } catch (IOException exception) {
            logger.severe("Failed to load config.properties: " + exception.getMessage());
        }
    }

    private void writeDefaults(File file) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            buildDefaults().store(stream, "OmniBans Sponge configuration");
        } catch (IOException exception) {
            logger.severe("Failed to write default config.properties: " + exception.getMessage());
        }
    }

    private Properties buildDefaults() {
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
        defaults.setProperty("server-name", "sponge");
        return defaults;
    }

    public InputStream openBundledStream() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            buildDefaults().store(buffer, null);
        } catch (IOException exception) {
            return null;
        }
        return new ByteArrayInputStream(buffer.toByteArray());
    }

    public boolean isMysql() { return "MYSQL".equalsIgnoreCase(properties.getProperty("storage.type", "SQLITE")); }
    public String getMysqlHost() { return properties.getProperty("storage.mysql.host", "localhost"); }
    public int getMysqlPort() { return Integer.parseInt(properties.getProperty("storage.mysql.port", "3306")); }
    public String getMysqlDatabase() { return properties.getProperty("storage.mysql.database", "omnibans"); }
    public String getMysqlUsername() { return properties.getProperty("storage.mysql.username", "root"); }
    public String getMysqlPassword() { return properties.getProperty("storage.mysql.password", ""); }
    public boolean isMysqlUseSsl() { return Boolean.parseBoolean(properties.getProperty("storage.mysql.use-ssl", "false")); }
    public boolean isRedisEnabled() { return Boolean.parseBoolean(properties.getProperty("redis.enabled", "false")); }
    public String getRedisHost() { return properties.getProperty("redis.host", "localhost"); }
    public int getRedisPort() { return Integer.parseInt(properties.getProperty("redis.port", "6379")); }
    public String getRedisPassword() { return properties.getProperty("redis.password", ""); }
    public String getServerName() { return properties.getProperty("server-name", "sponge"); }

}
