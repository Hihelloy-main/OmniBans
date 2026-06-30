package com.hihelloy.work.omnibans.common.punishment;

import java.util.UUID;

public final class Punishment {

    private final long id;
    private final PunishmentType type;
    private final PunishmentScope scope;
    private final String server;
    private final UUID targetUuid;
    private final String targetName;
    private final String targetIp;
    private final UUID staffUuid;
    private final String staffName;
    private final String reason;
    private final long createdAt;
    private final long expiresAt;
    private boolean active;
    private UUID removedByUuid;
    private String removedByName;
    private String removedReason;
    private long removedAt;

    private Punishment(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.scope = builder.scope;
        this.server = builder.server;
        this.targetUuid = builder.targetUuid;
        this.targetName = builder.targetName;
        this.targetIp = builder.targetIp;
        this.staffUuid = builder.staffUuid;
        this.staffName = builder.staffName;
        this.reason = builder.reason;
        this.createdAt = builder.createdAt;
        this.expiresAt = builder.expiresAt;
        this.active = builder.active;
        this.removedByUuid = builder.removedByUuid;
        this.removedByName = builder.removedByName;
        this.removedReason = builder.removedReason;
        this.removedAt = builder.removedAt;
    }

    public long getId() {
        return id;
    }

    public PunishmentType getType() {
        return type;
    }

    public PunishmentScope getScope() {
        return scope;
    }

    public String getServer() {
        return server;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getReason() {
        return reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt <= 0;
    }

    public boolean isExpired() {
        if (isPermanent()) {
            return false;
        }
        return System.currentTimeMillis() >= expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getRemovedByUuid() {
        return removedByUuid;
    }

    public void setRemovedByUuid(UUID removedByUuid) {
        this.removedByUuid = removedByUuid;
    }

    public String getRemovedByName() {
        return removedByName;
    }

    public void setRemovedByName(String removedByName) {
        this.removedByName = removedByName;
    }

    public String getRemovedReason() {
        return removedReason;
    }

    public void setRemovedReason(String removedReason) {
        this.removedReason = removedReason;
    }

    public long getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(long removedAt) {
        this.removedAt = removedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private long id;
        private PunishmentType type;
        private PunishmentScope scope = PunishmentScope.GLOBAL;
        private String server;
        private UUID targetUuid;
        private String targetName;
        private String targetIp;
        private UUID staffUuid;
        private String staffName;
        private String reason;
        private long createdAt = System.currentTimeMillis();
        private long expiresAt = -1L;
        private boolean active = true;
        private UUID removedByUuid;
        private String removedByName;
        private String removedReason;
        private long removedAt;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder type(PunishmentType type) {
            this.type = type;
            return this;
        }

        public Builder scope(PunishmentScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder server(String server) {
            this.server = server;
            return this;
        }

        public Builder targetUuid(UUID targetUuid) {
            this.targetUuid = targetUuid;
            return this;
        }

        public Builder targetName(String targetName) {
            this.targetName = targetName;
            return this;
        }

        public Builder targetIp(String targetIp) {
            this.targetIp = targetIp;
            return this;
        }

        public Builder staffUuid(UUID staffUuid) {
            this.staffUuid = staffUuid;
            return this;
        }

        public Builder staffName(String staffName) {
            this.staffName = staffName;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder expiresAt(long expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder removedByUuid(UUID removedByUuid) {
            this.removedByUuid = removedByUuid;
            return this;
        }

        public Builder removedByName(String removedByName) {
            this.removedByName = removedByName;
            return this;
        }

        public Builder removedReason(String removedReason) {
            this.removedReason = removedReason;
            return this;
        }

        public Builder removedAt(long removedAt) {
            this.removedAt = removedAt;
            return this;
        }

        public Punishment build() {
            return new Punishment(this);
        }

    }

}
