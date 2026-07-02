package com.hihelloy.work.omnibans.bungee.config;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public final class BungeeMessagesConfig {

    private final BungeeConfig config;
    private final File dataFolder;
    private final Logger logger;
    private Properties properties = new Properties();

    public BungeeMessagesConfig(BungeeConfig config, File dataFolder, Logger logger) {
        this.config = config;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void load() {
        File file = new File(dataFolder, "messages.properties");
        if (!file.exists()) {
            saveDefault(file);
        }
        properties = new Properties();
        try (FileInputStream stream = new FileInputStream(file)) {
            properties.load(stream);
        } catch (IOException exception) {
            logger.warning("Failed to load messages.properties, using bundled defaults: " + exception.getMessage());
            loadFromBundled();
        }
    }

    public BaseComponent[] components(String key) {
        return components(key, Collections.emptyMap());
    }

    public BaseComponent[] components(String key, Map<String, String> placeholders) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', raw(key, placeholders)));
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
            logger.warning("Failed to save default messages.properties: " + exception.getMessage());
        }
    }

    private void loadFromBundled() {
        try (InputStream stream = openBundledStream()) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException exception) {
            logger.severe("Failed to load bundled messages.properties: " + exception.getMessage());
        }
    }

}