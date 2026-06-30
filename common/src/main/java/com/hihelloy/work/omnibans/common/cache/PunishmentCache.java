package com.hihelloy.work.omnibans.common.cache;

import com.hihelloy.work.omnibans.common.punishment.Punishment;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PunishmentCache {

    private final Map<UUID, Punishment> activeBans = new ConcurrentHashMap<>();
    private final Map<UUID, Punishment> activeMutes = new ConcurrentHashMap<>();
    private final Map<String, Punishment> activeIpBans = new ConcurrentHashMap<>();
    private final Map<String, Punishment> activeIpMutes = new ConcurrentHashMap<>();
    private final Set<String> knownNames = ConcurrentHashMap.newKeySet();
    private final Set<String> bannedNames = ConcurrentHashMap.newKeySet();
    private final Set<String> mutedNames = ConcurrentHashMap.newKeySet();

    public void cacheBan(Punishment punishment) {
        if (punishment.getTargetUuid() != null) {
            activeBans.put(punishment.getTargetUuid(), punishment);
        }
        if (punishment.getTargetName() != null) {
            bannedNames.add(punishment.getTargetName());
        }
    }

    public void cacheMute(Punishment punishment) {
        if (punishment.getTargetUuid() != null) {
            activeMutes.put(punishment.getTargetUuid(), punishment);
        }
        if (punishment.getTargetName() != null) {
            mutedNames.add(punishment.getTargetName());
        }
    }

    public void cacheIpBan(Punishment punishment) {
        if (punishment.getTargetIp() != null) {
            activeIpBans.put(punishment.getTargetIp(), punishment);
        }
    }

    public void cacheIpMute(Punishment punishment) {
        if (punishment.getTargetIp() != null) {
            activeIpMutes.put(punishment.getTargetIp(), punishment);
        }
    }

    public void uncacheBan(UUID targetUuid) {
        Punishment removed = activeBans.remove(targetUuid);
        if (removed != null && removed.getTargetName() != null) {
            bannedNames.remove(removed.getTargetName());
        }
    }

    public void uncacheMute(UUID targetUuid) {
        Punishment removed = activeMutes.remove(targetUuid);
        if (removed != null && removed.getTargetName() != null) {
            mutedNames.remove(removed.getTargetName());
        }
    }

    public void uncacheIpBan(String ip) {
        activeIpBans.remove(ip);
    }

    public void uncacheIpMute(String ip) {
        activeIpMutes.remove(ip);
    }

    public Punishment getBan(UUID targetUuid) {
        return activeBans.get(targetUuid);
    }

    public Punishment getMute(UUID targetUuid) {
        return activeMutes.get(targetUuid);
    }

    public Punishment getIpBan(String ip) {
        return activeIpBans.get(ip);
    }

    public Punishment getIpMute(String ip) {
        return activeIpMutes.get(ip);
    }

    public void addKnownName(String name) {
        if (name != null) {
            knownNames.add(name);
        }
    }

    public Set<String> getKnownNames() {
        return Collections.unmodifiableSet(knownNames);
    }

    public Set<String> getBannedNames() {
        return Collections.unmodifiableSet(bannedNames);
    }

    public Set<String> getMutedNames() {
        return Collections.unmodifiableSet(mutedNames);
    }

    public void clear() {
        activeBans.clear();
        activeMutes.clear();
        activeIpBans.clear();
        activeIpMutes.clear();
        knownNames.clear();
        bannedNames.clear();
        mutedNames.clear();
    }

}
