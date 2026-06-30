package com.hihelloy.work.omnibans.util;

import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.util.logging.Logger;

public final class PaperLoggerAdapter implements PluginLogger {

    private final Logger logger;

    public PaperLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void severe(String message) {
        logger.severe(message);
    }

}
