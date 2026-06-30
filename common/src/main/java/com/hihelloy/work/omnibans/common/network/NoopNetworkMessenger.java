package com.hihelloy.work.omnibans.common.network;

import java.util.function.Consumer;

public final class NoopNetworkMessenger implements NetworkMessenger {

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void publish(NetworkPacket packet) {
    }

    @Override
    public void subscribe(Consumer<NetworkPacket> listener) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

}
