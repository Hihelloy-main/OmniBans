package com.hihelloy.work.omnibans.common.network;

import com.hihelloy.work.omnibans.common.util.PluginLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public final class RedisNetworkMessenger implements NetworkMessenger {

    private final String host;
    private final int port;
    private final String password;
    private final PluginLogger logger;
    private JedisPool pool;
    private Thread subscriberThread;
    private volatile boolean active;

    public RedisNetworkMessenger(String host, int port, String password, PluginLogger logger) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.logger = logger;
    }

    @Override
    public void connect() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(8);
        if (password == null || password.isBlank()) {
            pool = new JedisPool(config, host, port);
        } else {
            pool = new JedisPool(config, host, port, 2000, password);
        }
        active = true;
    }

    @Override
    public void disconnect() {
        active = false;
        if (subscriberThread != null) {
            subscriberThread.interrupt();
        }
        if (pool != null) {
            pool.close();
        }
    }

    @Override
    public void publish(NetworkPacket packet) {
        if (!active || pool == null) {
            return;
        }
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(NetworkChannel.REDIS_CHANNEL, packet.serialize());
        } catch (Exception exception) {
            logger.warn("Failed to publish network packet: " + exception.getMessage());
        }
    }

    @Override
    public void subscribe(Consumer<NetworkPacket> listener) {
        subscriberThread = new Thread(() -> runSubscriberLoop(listener), "OmniBans-Redis-Subscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }

    private void runSubscriberLoop(Consumer<NetworkPacket> listener) {
        while (active) {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(new PacketSubscriber(listener), NetworkChannel.REDIS_CHANNEL);
            } catch (Exception exception) {
                if (active) {
                    logger.warn("Redis subscription dropped, retrying: " + exception.getMessage());
                    sleepQuietly();
                }
            }
        }
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private final class PacketSubscriber extends JedisPubSub {

        private final Consumer<NetworkPacket> listener;

        private PacketSubscriber(Consumer<NetworkPacket> listener) {
            this.listener = listener;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                listener.accept(NetworkPacket.deserialize(message));
            } catch (Exception exception) {
                logger.warn("Failed to process network packet: " + exception.getMessage());
            }
        }

    }

}
