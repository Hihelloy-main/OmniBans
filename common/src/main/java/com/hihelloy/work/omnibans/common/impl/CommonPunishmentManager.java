package com.hihelloy.work.omnibans.common.impl;

import com.hihelloy.work.omnibans.api.event.OmniBansEventBus;
import com.hihelloy.work.omnibans.api.event.PostPunishmentEvent;
import com.hihelloy.work.omnibans.api.event.PrePunishmentEvent;
import com.hihelloy.work.omnibans.api.event.PunishmentEvent;
import com.hihelloy.work.omnibans.api.manager.PunishmentManager;
import com.hihelloy.work.omnibans.api.model.ApiPunishment;
import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;
import com.hihelloy.work.omnibans.api.request.PunishmentRequest;
import com.hihelloy.work.omnibans.common.cache.PunishmentCache;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.common.storage.PunishmentStorage;
import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public final class CommonPunishmentManager implements PunishmentManager {

    private final PunishmentStorage storage;
    private final PunishmentCache cache;
    private final OmniBansEventBus eventBus;
    private final Executor executor;
    private final String serverName;
    private final PluginLogger logger;
    private final Set<String> inFlightBans = ConcurrentHashMap.newKeySet();
    private final Set<String> inFlightMutes = ConcurrentHashMap.newKeySet();

    public CommonPunishmentManager(PunishmentStorage storage, PunishmentCache cache, OmniBansEventBus eventBus, Executor executor, String serverName, PluginLogger logger) {
        this.storage = storage;
        this.cache = cache;
        this.eventBus = eventBus;
        this.executor = executor;
        this.serverName = serverName;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<ApiPunishment> ban(PunishmentRequest request) {
        return applyPunishment(request, request.isIp() ? PunishmentType.IP_BAN : PunishmentType.BAN, inFlightBans);
    }

    @Override
    public CompletableFuture<ApiPunishment> mute(PunishmentRequest request) {
        return applyPunishment(request, request.isIp() ? PunishmentType.IP_MUTE : PunishmentType.MUTE, inFlightMutes);
    }

    @Override
    public CompletableFuture<ApiPunishment> kick(PunishmentRequest request) {
        return applyPunishment(request, PunishmentType.KICK, null);
    }

    @Override
    public CompletableFuture<ApiPunishment> warn(PunishmentRequest request) {
        return applyPunishment(request, PunishmentType.WARN, null);
    }

    @Override
    public CompletableFuture<ApiPunishment> note(PunishmentRequest request) {
        return applyPunishment(request, PunishmentType.NOTE, null);
    }

    private CompletableFuture<ApiPunishment> applyPunishment(PunishmentRequest request, PunishmentType type, Set<String> inFlight) {
        String flightKey = request.getTargetUuid() != null ? request.getTargetUuid().toString() : request.getTargetIp();
        if (inFlight != null && flightKey != null && !inFlight.add(flightKey)) {
            return CompletableFuture.completedFuture(null);
        }
        Punishment punishment = Punishment.builder()
            .type(type)
            .scope(PunishmentScope.GLOBAL)
            .server(serverName)
            .targetUuid(request.getTargetUuid())
            .targetName(request.getTargetName())
            .targetIp(request.getTargetIp())
            .staffUuid(request.getStaffUuid())
            .staffName(request.getStaffName())
            .reason(request.getReason())
            .expiresAt(request.getExpiresAt())
            .build();
        SimplePrePunishmentEvent preEvent = new SimplePrePunishmentEvent(punishment);
        eventBus.post(preEvent);
        if (preEvent.isCancelled()) {
            if (inFlight != null && flightKey != null) {
                inFlight.remove(flightKey);
            }
            return CompletableFuture.completedFuture(null);
        }
        return storage.insert(punishment).thenApply(inserted -> {
            if (inFlight != null && flightKey != null) {
                inFlight.remove(flightKey);
            }
            cacheInserted(inserted);
            CommonApiPunishment api = new CommonApiPunishment(inserted);
            eventBus.post(new SimplePostPunishmentEvent(api));
            return api;
        });
    }

    private void cacheInserted(Punishment punishment) {
        switch (punishment.getType()) {
            case BAN:
                cache.cacheBan(punishment);
                break;
            case IP_BAN:
                cache.cacheIpBan(punishment);
                break;
            case MUTE:
                cache.cacheMute(punishment);
                break;
            case IP_MUTE:
                cache.cacheIpMute(punishment);
                break;
            default:
                break;
        }
    }

    @Override
    public CompletableFuture<Boolean> unban(UUID targetUuid, UUID staffUuid, String staffName, String reason) {
        return storage.findActiveBan(targetUuid).thenCompose(p -> remove(p, staffUuid, staffName, reason));
    }

    @Override
    public CompletableFuture<Boolean> unbanIp(String ip, UUID staffUuid, String staffName, String reason) {
        return storage.findActiveIpBan(ip).thenCompose(p -> remove(p, staffUuid, staffName, reason));
    }

    @Override
    public CompletableFuture<Boolean> unmute(UUID targetUuid, UUID staffUuid, String staffName, String reason) {
        return storage.findActiveMute(targetUuid).thenCompose(p -> remove(p, staffUuid, staffName, reason));
    }

    @Override
    public CompletableFuture<Boolean> unmuteIp(String ip, UUID staffUuid, String staffName, String reason) {
        return storage.findActiveIpMute(ip).thenCompose(p -> remove(p, staffUuid, staffName, reason));
    }

    private CompletableFuture<Boolean> remove(Punishment punishment, UUID staffUuid, String staffName, String reason) {
        if (punishment == null) {
            return CompletableFuture.completedFuture(false);
        }
        punishment.setActive(false);
        punishment.setRemovedByUuid(staffUuid);
        punishment.setRemovedByName(staffName);
        punishment.setRemovedReason(reason);
        punishment.setRemovedAt(System.currentTimeMillis());
        return storage.update(punishment).thenApply(ignored -> true);
    }

    @Override
    public CompletableFuture<Optional<ApiPunishment>> getActiveBan(UUID targetUuid) {
        return storage.findActiveBan(targetUuid).thenApply(p -> Optional.ofNullable(p).map(CommonApiPunishment::new));
    }

    @Override
    public CompletableFuture<Optional<ApiPunishment>> getActiveIpBan(String ip) {
        return storage.findActiveIpBan(ip).thenApply(p -> Optional.ofNullable(p).map(CommonApiPunishment::new));
    }

    @Override
    public CompletableFuture<Optional<ApiPunishment>> getActiveMute(UUID targetUuid) {
        return storage.findActiveMute(targetUuid).thenApply(p -> Optional.ofNullable(p).map(CommonApiPunishment::new));
    }

    @Override
    public CompletableFuture<Optional<ApiPunishment>> getActiveIpMute(String ip) {
        return storage.findActiveIpMute(ip).thenApply(p -> Optional.ofNullable(p).map(CommonApiPunishment::new));
    }

    @Override
    public CompletableFuture<List<ApiPunishment>> getHistory(UUID targetUuid) {
        return storage.history(targetUuid).thenApply(list -> list.stream().map(CommonApiPunishment::new).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Integer> getActiveWarnCount(UUID targetUuid) {
        return storage.countActiveWarns(targetUuid);
    }

    private static final class SimplePrePunishmentEvent implements PrePunishmentEvent {

        private final Punishment punishment;
        private boolean cancelled;
        private String reason;
        private long expiresAt;

        private SimplePrePunishmentEvent(Punishment punishment) {
            this.punishment = punishment;
            this.reason = punishment.getReason();
            this.expiresAt = punishment.getExpiresAt();
        }

        @Override
        public ApiPunishmentType getPunishmentType() {
            return ApiPunishmentType.valueOf(punishment.getType().name());
        }

        @Override
        public UUID getTargetUuid() {
            return punishment.getTargetUuid();
        }

        @Override
        public String getTargetName() {
            return punishment.getTargetName();
        }

        @Override
        public UUID getStaffUuid() {
            return punishment.getStaffUuid();
        }

        @Override
        public String getStaffName() {
            return punishment.getStaffName();
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public void setReason(String reason) {
            this.reason = reason;
        }

        @Override
        public long getExpiresAt() {
            return expiresAt;
        }

        @Override
        public void setExpiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

    }

    private static final class SimplePostPunishmentEvent implements PostPunishmentEvent {

        private final CommonApiPunishment punishment;

        private SimplePostPunishmentEvent(CommonApiPunishment punishment) {
            this.punishment = punishment;
        }

        @Override
        public ApiPunishment getPunishment() {
            return punishment;
        }

        @Override
        public ApiPunishmentType getPunishmentType() {
            return punishment.getType();
        }

        @Override
        public UUID getTargetUuid() {
            return punishment.getTargetUuid();
        }

        @Override
        public String getTargetName() {
            return punishment.getTargetName();
        }

        @Override
        public UUID getStaffUuid() {
            return punishment.getStaffUuid();
        }

        @Override
        public String getStaffName() {
            return punishment.getStaffName();
        }

        @Override
        public String getReason() {
            return punishment.getReason();
        }

    }

}
