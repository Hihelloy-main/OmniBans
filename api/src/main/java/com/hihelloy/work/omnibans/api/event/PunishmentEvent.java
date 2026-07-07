package com.hihelloy.work.omnibans.api.event;

import com.hihelloy.work.omnibans.api.model.ApiPunishment;
import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;

import java.util.UUID;

/**
 * Base for both pre and post punishment events.
 */
public interface PunishmentEvent extends OmniBansEvent {

    ApiPunishmentType getPunishmentType();

    UUID getTargetUuid();

    String getTargetName();

    UUID getStaffUuid();

    String getStaffName();

    String getReason();

}
