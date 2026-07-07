package com.hihelloy.work.omnibans.neoforge;

import com.hihelloy.work.omnibans.common.util.PluginLogger;
import org.apache.logging.log4j.Logger;

public final class NeoForgePluginLogger implements PluginLogger {

    private final Logger logger;

    public NeoForgePluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void severe(String message) {
        logger.error(message);
    }

}
