package com.hihelloy.work.omnibans.api.event;

import java.util.function.Consumer;

/**
 * A simple typed event bus used to publish and subscribe to OmniBans events.
 * All events implement {@link OmniBansEvent}. Subscriptions are not thread safe,
 * register listeners during startup and not while the server is running.
 */
public interface OmniBansEventBus {

    /**
     * Subscribes {@code consumer} to receive every event of exactly {@code type}.
     */
    <E extends OmniBansEvent> void subscribe(Class<E> type, Consumer<E> consumer);

    /**
     * Removes a previously registered consumer for {@code type}.
     */
    <E extends OmniBansEvent> void unsubscribe(Class<E> type, Consumer<E> consumer);

    /**
     * Posts {@code event} to all subscribers registered for its runtime type.
     * Returns the same event after all subscribers have been called, allowing
     * the caller to inspect mutations (such as cancellation).
     */
    <E extends OmniBansEvent> E post(E event);

}
