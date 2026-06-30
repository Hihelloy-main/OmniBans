package com.hihelloy.work.omnibans.common.storage.sql;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.util.PluginLogger;
import com.hihelloy.work.omnibans.common.util.SeenAccount;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractSqlStorage implements PunishmentStorage {

    protected final Executor executor;
    protected final PluginLogger logger;
    protected HikariDataSource dataSource;

    protected AbstractSqlStorage(Executor executor, PluginLogger logger) {
        this.executor = executor;
        this.logger = logger;
    }

    protected abstract HikariConfig buildHikariConfig();

    protected abstract String createPunishmentsTableStatement();

    protected abstract String createSeenIpsTableStatement();

    @Override
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            HikariConfig config = buildHikariConfig();
            dataSource = new HikariDataSource(config);
            try (Connection connection = dataSource.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(createPunishmentsTableStatement());
                    statement.execute(createSeenIpsTableStatement());
                }
            } catch (SQLException exception) {
                throw new IllegalStateException("Failed to initialize OmniBans schema", exception);
            }
        }, executor);
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Override
    public CompletableFuture<Punishment> insert(Punishment punishment) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO omnibans_punishments (type, scope, server, target_uuid, target_name, target_ip, staff_uuid, staff_name, reason, created_at, expires_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, punishment.getType().name());
                    statement.setString(2, punishment.getScope().name());
                    statement.setString(3, punishment.getServer());
                    statement.setString(4, punishment.getTargetUuid() != null ? punishment.getTargetUuid().toString() : null);
                    statement.setString(5, punishment.getTargetName());
                    statement.setString(6, punishment.getTargetIp());
                    statement.setString(7, punishment.getStaffUuid() != null ? punishment.getStaffUuid().toString() : null);
                    statement.setString(8, punishment.getStaffName());
                    statement.setString(9, punishment.getReason());
                    statement.setLong(10, punishment.getCreatedAt());
                    statement.setLong(11, punishment.getExpiresAt());
                    statement.setBoolean(12, punishment.isActive());
                    statement.executeUpdate();
                    long id = 0L;
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            id = keys.getLong(1);
                        }
                    }
                    return Punishment.builder()
                        .id(id)
                        .type(punishment.getType())
                        .scope(punishment.getScope())
                        .server(punishment.getServer())
                        .targetUuid(punishment.getTargetUuid())
                        .targetName(punishment.getTargetName())
                        .targetIp(punishment.getTargetIp())
                        .staffUuid(punishment.getStaffUuid())
                        .staffName(punishment.getStaffName())
                        .reason(punishment.getReason())
                        .createdAt(punishment.getCreatedAt())
                        .expiresAt(punishment.getExpiresAt())
                        .active(punishment.isActive())
                        .build();
                }
            } catch (SQLException exception) {
                logger.severe("Failed to insert punishment: " + exception.getMessage());
                return punishment;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> update(Punishment punishment) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE omnibans_punishments SET active = ?, removed_by_uuid = ?, removed_by_name = ?, removed_reason = ?, removed_at = ?, expires_at = ? WHERE id = ?";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setBoolean(1, punishment.isActive());
                    statement.setString(2, punishment.getRemovedByUuid() != null ? punishment.getRemovedByUuid().toString() : null);
                    statement.setString(3, punishment.getRemovedByName());
                    statement.setString(4, punishment.getRemovedReason());
                    statement.setLong(5, punishment.getRemovedAt());
                    statement.setLong(6, punishment.getExpiresAt());
                    statement.setLong(7, punishment.getId());
                    statement.executeUpdate();
                }
            } catch (SQLException exception) {
                logger.severe("Failed to update punishment: " + exception.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Punishment> findActiveBan(UUID targetUuid) {
        return findActiveSingle("target_uuid", targetUuid.toString(), PunishmentType.BAN);
    }

    @Override
    public CompletableFuture<Punishment> findActiveIpBan(String ip) {
        return findActiveSingle("target_ip", ip, PunishmentType.IP_BAN);
    }

    @Override
    public CompletableFuture<Punishment> findActiveMute(UUID targetUuid) {
        return findActiveSingle("target_uuid", targetUuid.toString(), PunishmentType.MUTE);
    }

    @Override
    public CompletableFuture<Punishment> findActiveIpMute(String ip) {
        return findActiveSingle("target_ip", ip, PunishmentType.IP_MUTE);
    }

    private CompletableFuture<Punishment> findActiveSingle(String column, String value, PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM omnibans_punishments WHERE " + column + " = ? AND type = ? AND active = ? ORDER BY created_at DESC LIMIT 1";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, value);
                    statement.setString(2, type.name());
                    statement.setBoolean(3, true);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return mapRow(resultSet);
                        }
                        return null;
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to query punishment: " + exception.getMessage());
                return null;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<Punishment>> history(UUID targetUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Punishment> punishments = new ArrayList<>();
            String sql = "SELECT * FROM omnibans_punishments WHERE target_uuid = ? ORDER BY created_at DESC";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, targetUuid.toString());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            punishments.add(mapRow(resultSet));
                        }
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to query history: " + exception.getMessage());
            }
            return punishments;
        }, executor);
    }

    @Override
    public CompletableFuture<List<Punishment>> findActiveByType(PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> {
            List<Punishment> punishments = new ArrayList<>();
            String sql = "SELECT * FROM omnibans_punishments WHERE type = ? AND active = ? ORDER BY created_at DESC";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, type.name());
                    statement.setBoolean(2, true);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            punishments.add(mapRow(resultSet));
                        }
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to query active punishments: " + exception.getMessage());
            }
            return punishments;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> recordSeenIp(UUID uuid, String name, String ip) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO omnibans_seen_ips (target_uuid, target_name, ip, seen_at) VALUES (?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, name);
                    statement.setString(3, ip);
                    statement.setLong(4, System.currentTimeMillis());
                    statement.executeUpdate();
                }
            } catch (SQLException exception) {
                logger.severe("Failed to record seen ip: " + exception.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<String>> findKnownIps(UUID targetUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> ips = new ArrayList<>();
            String sql = "SELECT DISTINCT ip FROM omnibans_seen_ips WHERE target_uuid = ?";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, targetUuid.toString());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            ips.add(resultSet.getString("ip"));
                        }
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to query known ips: " + exception.getMessage());
            }
            return ips;
        }, executor);
    }

    @Override
    public CompletableFuture<List<String>> findAllKnownNames() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> names = new ArrayList<>();
            String sql = "SELECT DISTINCT target_name FROM omnibans_seen_ips WHERE target_name IS NOT NULL";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            names.add(resultSet.getString("target_name"));
                        }
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to query known names: " + exception.getMessage());
            }
            return names;
        }, executor);
    }

    @Override
    public CompletableFuture<List<SeenAccount>> findAccountsByIp(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, String> accounts = new LinkedHashMap<>();
            String sql = "SELECT DISTINCT target_uuid, target_name FROM omnibans_seen_ips WHERE ip = ?";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, ip);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            String raw = resultSet.getString("target_uuid");
                            if (raw != null) {
                                accounts.put(UUID.fromString(raw), resultSet.getString("target_name"));
                            }
                        }
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to query accounts by ip: " + exception.getMessage());
            }
            List<SeenAccount> result = new ArrayList<>();
            for (Map.Entry<UUID, String> entry : accounts.entrySet()) {
                result.add(new SeenAccount(entry.getKey(), entry.getValue()));
            }
            return result;
        }, executor);
    }

    @Override
    public CompletableFuture<Integer> countActiveWarns(UUID targetUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) AS total FROM omnibans_punishments WHERE target_uuid = ? AND type = ? AND active = ?";
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, targetUuid.toString());
                    statement.setString(2, PunishmentType.WARN.name());
                    statement.setBoolean(3, true);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt("total");
                        }
                        return 0;
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to count warns: " + exception.getMessage());
                return 0;
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<Punishment>> sweepExpired() {
        return CompletableFuture.supplyAsync(() -> {
            List<Punishment> expired = new ArrayList<>();
            String selectSql = "SELECT * FROM omnibans_punishments WHERE active = ? AND expires_at > 0 AND expires_at <= ?";
            String updateSql = "UPDATE omnibans_punishments SET active = ? WHERE id = ?";
            long now = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setBoolean(1, true);
                    statement.setLong(2, now);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            expired.add(mapRow(resultSet));
                        }
                    }
                }
                if (!expired.isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                        for (Punishment punishment : expired) {
                            statement.setBoolean(1, false);
                            statement.setLong(2, punishment.getId());
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }
            } catch (SQLException exception) {
                logger.severe("Failed to sweep expired punishments: " + exception.getMessage());
            }
            return expired;
        }, executor);
    }

    private Punishment mapRow(ResultSet resultSet) throws SQLException {
        String targetUuidRaw = resultSet.getString("target_uuid");
        String staffUuidRaw = resultSet.getString("staff_uuid");
        String removedByUuidRaw = resultSet.getString("removed_by_uuid");
        return Punishment.builder()
            .id(resultSet.getLong("id"))
            .type(PunishmentType.valueOf(resultSet.getString("type")))
            .scope(PunishmentScope.valueOf(resultSet.getString("scope")))
            .server(resultSet.getString("server"))
            .targetUuid(targetUuidRaw != null ? UUID.fromString(targetUuidRaw) : null)
            .targetName(resultSet.getString("target_name"))
            .targetIp(resultSet.getString("target_ip"))
            .staffUuid(staffUuidRaw != null ? UUID.fromString(staffUuidRaw) : null)
            .staffName(resultSet.getString("staff_name"))
            .reason(resultSet.getString("reason"))
            .createdAt(resultSet.getLong("created_at"))
            .expiresAt(resultSet.getLong("expires_at"))
            .active(resultSet.getBoolean("active"))
            .removedByUuid(removedByUuidRaw != null ? UUID.fromString(removedByUuidRaw) : null)
            .removedByName(resultSet.getString("removed_by_name"))
            .removedReason(resultSet.getString("removed_reason"))
            .removedAt(resultSet.getLong("removed_at"))
            .build();
    }

}
