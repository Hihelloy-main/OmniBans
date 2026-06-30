package com.hihelloy.work.omnibans.common.storage;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.common.util.SeenAccount;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Persistence layer for OmniBans punishments. Implementations must be safe
 * to call from any thread, every operation runs off the calling thread and
 * returns a {@link CompletableFuture}.
 */
public interface PunishmentStorage {

    /**
     * Initializes the storage backend, creating schema if required.
     */
    CompletableFuture<Void> connect();

    /**
     * Closes any open resources held by this storage backend.
     */
    void close();

    /**
     * Persists a new punishment and returns it with its generated id.
     */
    CompletableFuture<Punishment> insert(Punishment punishment);

    /**
     * Updates an existing punishment row, used when a punishment is removed.
     */
    CompletableFuture<Void> update(Punishment punishment);

    /**
     * Finds the active ban for the given uuid, if any.
     */
    CompletableFuture<Punishment> findActiveBan(UUID targetUuid);

    /**
     * Finds the active ban for the given ip address, if any.
     */
    CompletableFuture<Punishment> findActiveIpBan(String ip);

    /**
     * Finds the active mute for the given uuid, if any.
     */
    CompletableFuture<Punishment> findActiveMute(UUID targetUuid);

    /**
     * Finds the active mute for the given ip address, if any.
     */
    CompletableFuture<Punishment> findActiveIpMute(String ip);

    /**
     * Returns the full punishment history for a given player, newest first.
     */
    CompletableFuture<List<Punishment>> history(UUID targetUuid);

    /**
     * Returns every currently active punishment of the given type.
     */
    CompletableFuture<List<Punishment>> findActiveByType(PunishmentType type);

    /**
     * Records that a player connected from the given ip address, used for
     * alt account detection and ip ban resolution by player name.
     */
    CompletableFuture<Void> recordSeenIp(UUID uuid, String name, String ip);

    /**
     * Returns the distinct ip addresses on record for the given player.
     */
    CompletableFuture<List<String>> findKnownIps(UUID targetUuid);

    /**
     * Returns every distinct player name OmniBans has ever seen, used to
     * seed tab completion caches on startup.
     */
    CompletableFuture<List<String>> findAllKnownNames();

    /**
     * Returns the distinct player accounts on record for the given ip address.
     */
    CompletableFuture<List<SeenAccount>> findAccountsByIp(String ip);

    /**
     * Counts active warnings for a given player.
     */
    CompletableFuture<Integer> countActiveWarns(UUID targetUuid);

    /**
     * Sweeps and deactivates every punishment whose expiry has passed,
     * returning the punishments that were just expired.
     */
    CompletableFuture<List<Punishment>> sweepExpired();

}
