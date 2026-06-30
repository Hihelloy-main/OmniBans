package com.hihelloy.work.omnibans.config;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.text.MessageFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class MessagesConfig {

    private final OmniBans plugin;
    private final MessageFormatter formatter;
    private YamlConfiguration configuration;

    public MessagesConfig(OmniBans plugin) {
        this.plugin = plugin;
        this.formatter = new MessageFormatter();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                configuration.setDefaults(defaults);
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to load default messages: " + exception.getMessage());
        }
    }

    public String getPrefix() {
        return plugin.getOmniBansConfig().getPrefix();
    }

    public String raw(String path) {
        return configuration.getString(path, path);
    }

    public Component component(String path) {
        return component(path, Map.of());
    }

    public Component component(String path, Map<String, String> placeholders) {
        String resolved = apply(raw(path), placeholders);
        return formatter.parse(resolved);
    }

    public String text(String path, Map<String, String> placeholders) {
        return apply(raw(path), placeholders);
    }

    private String apply(String input, Map<String, String> placeholders) {
        String result = input.replace("{prefix}", getPrefix());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

}
