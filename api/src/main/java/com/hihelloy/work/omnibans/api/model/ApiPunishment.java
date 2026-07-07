package com.hihelloy.work.omnibans.api.model;

import java.util.UUID;

/**
 * An immutable snapshot of a punishment record as stored by OmniBans.
 * The values here reflect the state at the time this snapshot was created
 * and will not change even if the underlying record is later removed.
 */
public interface ApiPunishment {

    long getId();

    ApiPunishmentType getType();

    UUID getTargetUuid();

    String getTargetName();

    String getTargetIp();

    UUID getStaffUuid();

    String getStaffName();

    String getReason();

    long getCreatedAt();

    /** Returns the expiry timestamp in milliseconds, or -1 for permanent punishments. */
    long getExpiresAt();

    boolean isPermanent();

    boolean isExpired();

    boolean isActive();

}
