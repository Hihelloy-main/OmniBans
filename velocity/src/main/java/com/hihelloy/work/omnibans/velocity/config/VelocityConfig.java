package com.hihelloy.work.omnibans.velocity.config;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class VelocityConfig {

    private final Path dataDirectory;
    private final Logger logger;
    private final Properties properties = new Properties();

    public VelocityConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void load() {
        try {
            Files.createDirectories(dataDirectory);
            Path configPath = dataDirectory.resolve("config.properties");
            if (!Files.exists(configPath)) {
                writeDefaults(configPath);
            }
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            logger.error("Failed to load OmniBans configuration", exception);
        }
    }

    private void writeDefaults(Path configPath) throws IOException {
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
        defaults.setProperty("prefix", "<gray>[<red>OmniBans</red><gray>]");
        try (OutputStream outputStream = Files.newOutputStream(configPath)) {
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
        return properties.getProperty("prefix", "<gray>[<red>OmniBans</red><gray>]");
    }

}
