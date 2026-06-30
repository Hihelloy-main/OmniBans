package com.hihelloy.work.omnibans.common.network;

import java.util.function.Consumer;

public interface NetworkMessenger {

    void connect();

    void disconnect();

    void publish(NetworkPacket packet);

    void subscribe(Consumer<NetworkPacket> listener);

    boolean isActive();

}
