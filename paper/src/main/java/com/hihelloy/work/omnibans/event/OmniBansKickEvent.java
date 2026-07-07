package com.hihelloy.work.omnibans.event;

import com.hihelloy.work.omnibans.common.punishment.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired by OmniBans on the main thread after a kick is stored and the
 * in-game effects (kick / notify) have been scheduled. This event is
 * informational only and cannot be cancelled; use the API's
 * {@link com.hihelloy.work.omnibans.api.event.PrePunishmentEvent} to cancel.
 */
public final class OmniBansKickEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Punishment punishment;

    public OmniBansKickEvent(Punishment punishment) {
        super(true);
        this.punishment = punishment;
    }

    public Punishment getPunishment() {
        return punishment;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
