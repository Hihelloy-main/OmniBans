package com.hihelloy.work.omnibans.velocity.util;

import com.hihelloy.work.omnibans.common.util.PluginLogger;
import org.slf4j.Logger;

public final class VelocityLoggerAdapter implements PluginLogger {

    private final Logger logger;

    public VelocityLoggerAdapter(Logger logger) {
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
