package com.hihelloy.work.omnibans.fabric;

import com.hihelloy.work.omnibans.common.util.PluginLogger;
import org.slf4j.Logger;

public final class FabricPluginLogger implements PluginLogger {

    private final Logger logger;

    public FabricPluginLogger(Logger logger) {
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
