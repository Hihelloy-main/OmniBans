package com.hihelloy.work.omnibans.api.event;

import com.hihelloy.work.omnibans.api.model.ApiPunishment;

/**
 * Fired after a punishment has been successfully written to storage.
 * The punishment cannot be cancelled at this stage.
 */
public interface PostPunishmentEvent extends PunishmentEvent {

    ApiPunishment getPunishment();

}
