package com.hihelloy.work.omnibans.velocity.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public final class VelocityMessagesConfig {

    private final VelocityConfig config;
    private final Path dataDirectory;
    private final MiniMessage miniMessage;
    private final org.slf4j.Logger logger;
    private Properties properties = new Properties();

    public VelocityMessagesConfig(VelocityConfig config, Path dataDirectory, org.slf4j.Logger logger) {
        this.config = config;
        this.dataDirectory = dataDirectory;
        this.miniMessage = MiniMessage.miniMessage();
        this.logger = logger;
    }

    public void load() {
        File file = dataDirectory.resolve("messages.properties").toFile();
        if (!file.exists()) {
            saveDefault(file);
        }
        properties = new Properties();
        try (FileInputStream stream = new FileInputStream(file)) {
            properties.load(stream);
        } catch (IOException exception) {
            logger.warn("Failed to load messages.properties, using bundled defaults: " + exception.getMessage());
            loadFromBundled();
        }
    }

    public Component component(String key) {
        return component(key, Collections.emptyMap());
    }

    public Component component(String key, Map<String, String> placeholders) {
        return miniMessage.deserialize(raw(key, placeholders));
    }

    public String raw(String key) {
        return raw(key, Collections.emptyMap());
    }

    public String raw(String key, Map<String, String> placeholders) {
        String value = properties.getProperty(key, key);
        value = value.replace("{prefix}", config.getPrefix());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return value;
    }

    public InputStream openBundledStream() {
        return getClass().getClassLoader().getResourceAsStream("messages.properties");
    }

    private void saveDefault(File file) {
        try (InputStream stream = openBundledStream()) {
            if (stream != null) {
                Files.copy(stream, file.toPath());
            }
        } catch (IOException exception) {
            logger.warn("Failed to save default messages.properties: " + exception.getMessage());
        }
    }

    private void loadFromBundled() {
        try (InputStream stream = openBundledStream()) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException exception) {
            logger.error("Failed to load bundled messages.properties: " + exception.getMessage());
        }
    }

}