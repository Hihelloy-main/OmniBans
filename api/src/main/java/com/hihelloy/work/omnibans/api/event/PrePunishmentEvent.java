package com.hihelloy.work.omnibans.api.event;

/**
 * Fired before a punishment is written to storage. Subscribers may cancel this event
 * to prevent the punishment from being applied, or modify the reason or expiry.
 */
public interface PrePunishmentEvent extends PunishmentEvent {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

    void setReason(String reason);

    void setExpiresAt(long expiresAt);

    long getExpiresAt();

}
