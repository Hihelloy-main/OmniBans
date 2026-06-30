package com.hihelloy.work.omnibans.service;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.SeenAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class AltLookupService {

    private final OmniBans plugin;

    public AltLookupService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Map<UUID, String>> findAlts(UUID targetUuid) {
        return plugin.getStorage().findKnownIps(targetUuid).thenCompose(ips -> findAltsForIps(targetUuid, ips));
    }

    private CompletableFuture<Map<UUID, String>> findAltsForIps(UUID targetUuid, List<String> ips) {
        List<CompletableFuture<List<SeenAccount>>> futures = new ArrayList<>();
        for (String ip : ips) {
            futures.add(plugin.getStorage().findAccountsByIp(ip));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(ignored -> {
            Map<UUID, String> altMap = new LinkedHashMap<>();
            for (CompletableFuture<List<SeenAccount>> future : futures) {
                for (SeenAccount account : future.join()) {
                    if (!account.getUuid().equals(targetUuid)) {
                        altMap.put(account.getUuid(), account.getName());
                    }
                }
            }
            return altMap;
        });
    }

    public CompletableFuture<List<String>> findBannedAltNames(Map<UUID, String> altMap) {
        List<UUID> uuids = new ArrayList<>(altMap.keySet());
        List<CompletableFuture<Punishment>> futures = new ArrayList<>();
        for (UUID uuid : uuids) {
            futures.add(plugin.getStorage().findActiveBan(uuid));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(ignored -> {
            List<String> bannedAlts = new ArrayList<>();
            for (int index = 0; index < uuids.size(); index++) {
                Punishment ban = futures.get(index).join();
                if (ban != null && !ban.isExpired()) {
                    bannedAlts.add(altMap.get(uuids.get(index)));
                }
            }
            return bannedAlts;
        });
    }

}
