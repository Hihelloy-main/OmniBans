package com.hihelloy.work.omnibans.common.impl;

import com.hihelloy.work.omnibans.api.manager.PlayerManager;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.util.SeenAccount;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class CommonPlayerManager implements PlayerManager {

    private final PunishmentStorage storage;

    public CommonPlayerManager(PunishmentStorage storage) {
        this.storage = storage;
    }

    @Override
    public CompletableFuture<List<String>> getKnownIps(UUID playerUuid) {
        return storage.findKnownIps(playerUuid);
    }

    @Override
    public CompletableFuture<List<AltAccount>> getAlts(UUID playerUuid) {
        return storage.findKnownIps(playerUuid).thenCompose(ips -> resolveAlts(playerUuid, ips));
    }

    private CompletableFuture<List<AltAccount>> resolveAlts(UUID playerUuid, List<String> ips) {
        List<CompletableFuture<List<SeenAccount>>> futures = ips.stream()
            .map(storage::findAccountsByIp)
            .collect(Collectors.toList());
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(ignored ->
            futures.stream()
                .flatMap(future -> future.join().stream())
                .filter(account -> !account.getUuid().equals(playerUuid))
                .map(account -> (AltAccount) new AltAccountImpl(account.getUuid(), account.getName()))
                .distinct()
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Void> recordIp(UUID playerUuid, String playerName, String ip) {
        return storage.recordSeenIp(playerUuid, playerName, ip);
    }

    private static final class AltAccountImpl implements AltAccount {

        private final UUID uuid;
        private final String name;

        private AltAccountImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public String getName() {
            return name;
        }

    }

}
