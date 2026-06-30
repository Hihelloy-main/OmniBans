package com.hihelloy.work.omnibans.common.storage.sql;

import com.hihelloy.work.omnibans.common.util.PluginLogger;
import com.zaxxer.hikari.HikariConfig;

import java.util.concurrent.Executor;

public final class MySqlStorage extends AbstractSqlStorage {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSsl;

    public MySqlStorage(String host, int port, String database, String username, String password, boolean useSsl, Executor executor, PluginLogger logger) {
        super(executor, logger);
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.useSsl = useSsl;
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSsl + "&autoReconnect=true&characterEncoding=utf8";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setPoolName("OmniBans-MySQL");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }

    @Override
    protected String createPunishmentsTableStatement() {
        return "CREATE TABLE IF NOT EXISTS omnibans_punishments (" +
            "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
            "type VARCHAR(32) NOT NULL, " +
            "scope VARCHAR(16) NOT NULL, " +
            "server VARCHAR(64), " +
            "target_uuid VARCHAR(36), " +
            "target_name VARCHAR(32), " +
            "target_ip VARCHAR(64), " +
            "staff_uuid VARCHAR(36), " +
            "staff_name VARCHAR(32), " +
            "reason VARCHAR(512), " +
            "created_at BIGINT NOT NULL, " +
            "expires_at BIGINT NOT NULL, " +
            "active BOOLEAN NOT NULL, " +
            "removed_by_uuid VARCHAR(36), " +
            "removed_by_name VARCHAR(32), " +
            "removed_reason VARCHAR(512), " +
            "removed_at BIGINT, " +
            "INDEX idx_target_uuid (target_uuid), " +
            "INDEX idx_target_ip (target_ip), " +
            "INDEX idx_type_active (type, active))";
    }

    @Override
    protected String createSeenIpsTableStatement() {
        return "CREATE TABLE IF NOT EXISTS omnibans_seen_ips (" +
            "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
            "target_uuid VARCHAR(36) NOT NULL, " +
            "target_name VARCHAR(32), " +
            "ip VARCHAR(64) NOT NULL, " +
            "seen_at BIGINT NOT NULL, " +
            "INDEX idx_seen_uuid (target_uuid), " +
            "INDEX idx_seen_ip (ip))";
    }

}
