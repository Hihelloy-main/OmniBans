package com.hihelloy.work.omnibans.common.network;

import java.util.UUID;

public final class NetworkPacket {

    private static final String DELIMITER = "\u0001";

    private final NetworkAction action;
    private final long punishmentId;
    private final String punishmentType;
    private final UUID targetUuid;
    private final String targetName;
    private final String originServer;

    public NetworkPacket(NetworkAction action, long punishmentId, String punishmentType, UUID targetUuid, String targetName, String originServer) {
        this.action = action;
        this.punishmentId = punishmentId;
        this.punishmentType = punishmentType;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.originServer = originServer;
    }

    public NetworkAction getAction() {
        return action;
    }

    public long getPunishmentId() {
        return punishmentId;
    }

    public String getPunishmentType() {
        return punishmentType;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getOriginServer() {
        return originServer;
    }

    public String serialize() {
        String uuidPart = targetUuid != null ? targetUuid.toString() : "";
        String namePart = targetName != null ? targetName : "";
        String serverPart = originServer != null ? originServer : "";
        return action.name() + DELIMITER + punishmentId + DELIMITER + punishmentType + DELIMITER + uuidPart + DELIMITER + namePart + DELIMITER + serverPart;
    }

    public static NetworkPacket deserialize(String raw) {
        String[] parts = raw.split(DELIMITER);
        NetworkAction action = NetworkAction.valueOf(parts[0]);
        long punishmentId = Long.parseLong(parts[1]);
        String punishmentType = parts[2];
        UUID targetUuid = parts.length > 3 && !parts[3].isEmpty() ? UUID.fromString(parts[3]) : null;
        String targetName = parts.length > 4 ? parts[4] : "";
        String originServer = parts.length > 5 ? parts[5] : "";
        return new NetworkPacket(action, punishmentId, punishmentType, targetUuid, targetName, originServer);
    }

}
