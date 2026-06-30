package com.hihelloy.work.omnibans.common.util;

import java.util.UUID;

public final class SeenAccount {

    private final UUID uuid;
    private final String name;

    public SeenAccount(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

}
