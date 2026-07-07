package com.hihelloy.work.omnibans.config;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.gui.ConfigField;
import com.hihelloy.work.omnibans.gui.YamlFlattener;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class ConfigMigrationService {

    private static final List<String> REMOVED_CONFIG_KEYS = List.of();
    private static final List<String> REMOVED_MESSAGE_KEYS = List.of();

    private final OmniBans plugin;

    public ConfigMigrationService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void migrateConfig() {
        migrate("config.yml", REMOVED_CONFIG_KEYS);
    }

    public void migrateMessages() {
        ensureExists("messages.yml");
        migrate("messages.yml", REMOVED_MESSAGE_KEYS);
    }

    private void ensureExists(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    private void migrate(String fileName, List<String> removedKeys) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            return;
        }
        YamlConfiguration onDisk = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration bundledDefault = loadBundledDefault(fileName);
        if (bundledDefault == null) {
            return;
        }
        boolean changed = false;
        for (ConfigField field : YamlFlattener.flatten(bundledDefault)) {
            if (!onDisk.contains(field.getPath())) {
                onDisk.set(field.getPath(), bundledDefault.get(field.getPath()));
                changed = true;
            }
        }
        for (String removedKey : removedKeys) {
            if (onDisk.contains(removedKey)) {
                onDisk.set(removedKey, null);
                plugin.getLogger().info("Removed the no longer used " + fileName + " setting '" + removedKey + "', this is expected after an update and safe to ignore.");
                changed = true;
            }
        }
        if (changed) {
            saveWithBackup(file, onDisk);
        }
    }

    private YamlConfiguration loadBundledDefault(String fileName) {
        try (InputStream stream = plugin.getResource(fileName)) {
            if (stream == null) {
                return null;
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to read the bundled " + fileName + " for migration: " + exception.getMessage());
            return null;
        }
    }

    private void saveWithBackup(File file, YamlConfiguration updated) {
        File backup = new File(file.getParentFile(), file.getName() + ".bak");
        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to back up " + file.getName() + " before migrating it: " + exception.getMessage());
        }
        try {
            updated.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save the migrated " + file.getName() + ": " + exception.getMessage());
        }
    }

}
