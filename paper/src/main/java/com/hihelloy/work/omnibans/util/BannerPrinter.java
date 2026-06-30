package com.hihelloy.work.omnibans.util;

import com.hihelloy.work.omnibans.OmniBans;

import java.util.logging.Logger;

public final class BannerPrinter {

    private final OmniBans plugin;

    public BannerPrinter(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void print() {
        Logger logger = plugin.getLogger();
        String version = plugin.getDescription().getVersion();
        String platform = detectPlatform();
        String serverName = plugin.getOmniBansConfig().getServerName();
        String storage = plugin.getOmniBansConfig().getStorageType().name();
        String redis = plugin.getOmniBansConfig().isRedisEnabled() ? "Enabled" : "Disabled";
        String discord = plugin.getOmniBansConfig().isDiscordBotEnabled() ? "Enabled" : "Disabled";
        logger.info("");
        logger.info("   ___                   _ ____  ");
        logger.info("  / _ \\ _ __ ___  _ __ (_) __ ) __ _ _ __  ___ ");
        logger.info(" | | | | '_ ` _ \\| '_ \\| |  _ \\/ _` | '_ \\/ __|");
        logger.info(" | |_| | | | | | | | | | | |_) | (_| | | | \\__ \\");
        logger.info("  \\___/|_| |_| |_|_| |_|_|____/ \\__,_|_| |_|___/");
        logger.info("");
        logger.info("  v" + version + "  |  Running on " + platform + " - " + serverName);
        logger.info("  Storage: " + storage + "  |  Redis: " + redis + "  |  Discord Bot: " + discord);
        logger.info("");
    }

    private String detectPlatform() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return "Folia";
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("io.papermc.paper.Paper");
            return "Paper";
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return "Paper";
        } catch (ClassNotFoundException e) {
        }
        return "Spigot";
    }

}