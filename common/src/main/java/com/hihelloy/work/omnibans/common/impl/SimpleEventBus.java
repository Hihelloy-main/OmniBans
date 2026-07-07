package com.hihelloy.work.omnibans.common.impl;

import com.hihelloy.work.omnibans.api.event.OmniBansEvent;
import com.hihelloy.work.omnibans.api.event.OmniBansEventBus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class SimpleEventBus implements OmniBansEventBus {

    private final Map<Class<?>, List<Consumer<OmniBansEvent>>> listeners = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <E extends OmniBansEvent> void subscribe(Class<E> type, Consumer<E> consumer) {
        listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add((Consumer<OmniBansEvent>) consumer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends OmniBansEvent> void unsubscribe(Class<E> type, Consumer<E> consumer) {
        List<Consumer<OmniBansEvent>> list = listeners.get(type);
        if (list != null) {
            list.remove(consumer);
        }
    }

    @Override
    public <E extends OmniBansEvent> E post(E event) {
        List<Consumer<OmniBansEvent>> list = listeners.get(event.getClass());
        if (list != null) {
            for (Consumer<OmniBansEvent> consumer : list) {
                consumer.accept(event);
            }
        }
        return event;
    }

}
