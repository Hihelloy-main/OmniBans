package com.hihelloy.work.omnibans.common.config;

import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

public final class ProxyMigrationService {

    private final PluginLogger logger;

    public ProxyMigrationService(PluginLogger logger) {
        this.logger = logger;
    }

    public void migrate(File file, InputStream bundledDefault, List<String> removedKeys) {
        if (!file.exists() || bundledDefault == null) {
            return;
        }
        Properties onDisk = loadFile(file);
        if (onDisk == null) {
            return;
        }
        Properties bundled = loadStream(bundledDefault);
        if (bundled == null) {
            return;
        }
        boolean changed = false;
        for (String key : bundled.stringPropertyNames()) {
            if (!onDisk.containsKey(key)) {
                onDisk.setProperty(key, bundled.getProperty(key));
                changed = true;
            }
        }
        for (String removedKey : removedKeys) {
            if (onDisk.containsKey(removedKey)) {
                onDisk.remove(removedKey);
                logger.info("Removed the no longer used setting '" + removedKey + "' from " + file.getName() + ", this is expected after an update.");
                changed = true;
            }
        }
        if (!changed) {
            return;
        }
        backup(file);
        save(file, onDisk);
    }

    private Properties loadFile(File file) {
        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(file)) {
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            logger.warn("Failed to load " + file.getName() + " for migration: " + exception.getMessage());
            return null;
        }
    }

    private Properties loadStream(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            logger.warn("Failed to read bundled default during migration: " + exception.getMessage());
            return null;
        }
    }

    private void backup(File file) {
        File backup = new File(file.getParentFile(), file.getName() + ".bak");
        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            logger.warn("Failed to back up " + file.getName() + " before migrating: " + exception.getMessage());
        }
    }

    private void save(File file, Properties properties) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            properties.store(stream, "OmniBans auto-migrated properties");
        } catch (IOException exception) {
            logger.warn("Failed to save migrated " + file.getName() + ": " + exception.getMessage());
        }
    }

}