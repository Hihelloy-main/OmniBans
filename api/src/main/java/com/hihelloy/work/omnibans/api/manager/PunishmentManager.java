package com.hihelloy.work.omnibans.api.manager;

import com.hihelloy.work.omnibans.api.model.ApiPunishment;
import com.hihelloy.work.omnibans.api.request.PunishmentRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The primary API surface for issuing and querying punishments. Every method
 * that modifies state fires a {@link com.hihelloy.work.omnibans.api.event.PrePunishmentEvent}
 * before storage and a {@link com.hihelloy.work.omnibans.api.event.PostPunishmentEvent} after.
 * Returning {@code null} from a modify operation means a subscriber cancelled it
 * via the pre-event.
 */
public interface PunishmentManager {

    CompletableFuture<ApiPunishment> ban(PunishmentRequest request);

    CompletableFuture<ApiPunishment> mute(PunishmentRequest request);

    CompletableFuture<ApiPunishment> kick(PunishmentRequest request);

    CompletableFuture<ApiPunishment> warn(PunishmentRequest request);

    CompletableFuture<ApiPunishment> note(PunishmentRequest request);

    CompletableFuture<Boolean> unban(UUID targetUuid, UUID staffUuid, String staffName, String reason);

    CompletableFuture<Boolean> unbanIp(String ip, UUID staffUuid, String staffName, String reason);

    CompletableFuture<Boolean> unmute(UUID targetUuid, UUID staffUuid, String staffName, String reason);

    CompletableFuture<Boolean> unmuteIp(String ip, UUID staffUuid, String staffName, String reason);

    CompletableFuture<Optional<ApiPunishment>> getActiveBan(UUID targetUuid);

    CompletableFuture<Optional<ApiPunishment>> getActiveIpBan(String ip);

    CompletableFuture<Optional<ApiPunishment>> getActiveMute(UUID targetUuid);

    CompletableFuture<Optional<ApiPunishment>> getActiveIpMute(String ip);

    CompletableFuture<List<ApiPunishment>> getHistory(UUID targetUuid);

    CompletableFuture<Integer> getActiveWarnCount(UUID targetUuid);

}
