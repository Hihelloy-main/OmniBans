package com.hihelloy.work.omnibans.api.manager;

import com.hihelloy.work.omnibans.api.model.ApiPunishment;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides access to player-related data stored by OmniBans such as known ip
 * addresses and alt account lookups.
 */
public interface PlayerManager {

    /**
     * Returns every distinct ip address on record for the given player. Never
     * includes raw addresses in display contexts; use the count rather than the
     * values when building player-facing output.
     */
    CompletableFuture<List<String>> getKnownIps(UUID playerUuid);

    /**
     * Returns the UUIDs and names of every account OmniBans has seen connecting
     * from any address also used by {@code playerUuid}.
     */
    CompletableFuture<List<AltAccount>> getAlts(UUID playerUuid);

    /**
     * Records that a player with the given identity connected from the given
     * address. This is called automatically by OmniBans on every join; only
     * call it directly if you are backfilling data from a migration.
     */
    CompletableFuture<Void> recordIp(UUID playerUuid, String playerName, String ip);

    interface AltAccount {

        UUID getUuid();

        String getName();

    }

}
