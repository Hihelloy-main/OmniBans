package com.hihelloy.work.omnibans.common.storage.sql;

import com.hihelloy.work.omnibans.common.util.PluginLogger;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;
import java.util.concurrent.Executor;

public final class SqliteStorage extends AbstractSqlStorage {

    private final File databaseFile;

    public SqliteStorage(File databaseFile, Executor executor, PluginLogger logger) {
        super(executor, logger);
        this.databaseFile = databaseFile;
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setPoolName("OmniBans-SQLite");
        return config;
    }

    @Override
    protected String createPunishmentsTableStatement() {
        return "CREATE TABLE IF NOT EXISTS omnibans_punishments (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "type TEXT NOT NULL, " +
            "scope TEXT NOT NULL, " +
            "server TEXT, " +
            "target_uuid TEXT, " +
            "target_name TEXT, " +
            "target_ip TEXT, " +
            "staff_uuid TEXT, " +
            "staff_name TEXT, " +
            "reason TEXT, " +
            "created_at INTEGER NOT NULL, " +
            "expires_at INTEGER NOT NULL, " +
            "active INTEGER NOT NULL, " +
            "removed_by_uuid TEXT, " +
            "removed_by_name TEXT, " +
            "removed_reason TEXT, " +
            "removed_at INTEGER)";
    }

    @Override
    protected String createSeenIpsTableStatement() {
        return "CREATE TABLE IF NOT EXISTS omnibans_seen_ips (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "target_uuid TEXT NOT NULL, " +
            "target_name TEXT, " +
            "ip TEXT NOT NULL, " +
            "seen_at INTEGER NOT NULL)";
    }

}
