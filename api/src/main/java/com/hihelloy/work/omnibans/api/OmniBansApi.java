package com.hihelloy.work.omnibans.api;

import com.hihelloy.work.omnibans.api.event.OmniBansEventBus;
import com.hihelloy.work.omnibans.api.manager.PlayerManager;
import com.hihelloy.work.omnibans.api.manager.PunishmentManager;
import com.hihelloy.work.omnibans.api.platform.Platform;

/**
 * The main OmniBans API entry point. Obtain an instance via {@link OmniBansProvider#get()}.
 *
 * <p>Example usage:
 * <pre>{@code
 * OmniBansApi api = OmniBansProvider.get();
 * api.getPunishmentManager().ban(
 *     PunishmentRequest.builder(ApiPunishmentType.BAN)
 *         .target(uuid, name)
 *         .staff(staffUuid, "Hihelloy")
 *         .reason("Hacking")
 *         .permanent()
 *         .build()
 * ).thenAccept(punishment -> System.out.println("Banned: " + punishment.getId()));
 * }</pre>
 */
public interface OmniBansApi {

    PunishmentManager getPunishmentManager();

    PlayerManager getPlayerManager();

    OmniBansEventBus getEventBus();

    Platform getPlatform();

    String getVersion();

}
