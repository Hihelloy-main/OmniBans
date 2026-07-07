package com.hihelloy.work.omnibans.api.request;

import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;

import java.util.UUID;

/**
 * An immutable request to issue a punishment. Build one with {@link #builder(ApiPunishmentType)}.
 */
public final class PunishmentRequest {

    private final ApiPunishmentType type;
    private final UUID targetUuid;
    private final String targetName;
    private final String targetIp;
    private final UUID staffUuid;
    private final String staffName;
    private final String reason;
    private final long expiresAt;
    private final boolean ip;

    private PunishmentRequest(Builder builder) {
        this.type = builder.type;
        this.targetUuid = builder.targetUuid;
        this.targetName = builder.targetName;
        this.targetIp = builder.targetIp;
        this.staffUuid = builder.staffUuid;
        this.staffName = builder.staffName;
        this.reason = builder.reason;
        this.expiresAt = builder.expiresAt;
        this.ip = builder.ip;
    }

    public static Builder builder(ApiPunishmentType type) {
        return new Builder(type);
    }

    public ApiPunishmentType getType() {
        return type;
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

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isIp() {
        return ip;
    }

    public static final class Builder {

        private final ApiPunishmentType type;
        private UUID targetUuid;
        private String targetName;
        private String targetIp;
        private UUID staffUuid;
        private String staffName = "Console";
        private String reason = "No reason specified";
        private long expiresAt = -1L;
        private boolean ip = false;

        private Builder(ApiPunishmentType type) {
            this.type = type;
        }

        public Builder target(UUID uuid, String name) {
            this.targetUuid = uuid;
            this.targetName = name;
            return this;
        }

        public Builder targetIp(String ip) {
            this.targetIp = ip;
            this.ip = true;
            return this;
        }

        public Builder staff(UUID uuid, String name) {
            this.staffUuid = uuid;
            this.staffName = name;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder expiresAt(long timestamp) {
            this.expiresAt = timestamp;
            return this;
        }

        public Builder duration(String durationString) {
            long millis = parseDuration(durationString);
            if (millis > 0) {
                this.expiresAt = System.currentTimeMillis() + millis;
            }
            return this;
        }

        private static long parseDuration(String input) {
            if (input == null || input.trim().isEmpty()) {
                return 0L;
            }
            long total = 0L;
            int index = 0;
            String lower = input.toLowerCase(java.util.Locale.ROOT);
            while (index < lower.length()) {
                StringBuilder digits = new StringBuilder();
                while (index < lower.length() && Character.isDigit(lower.charAt(index))) {
                    digits.append(lower.charAt(index++));
                }
                if (digits.length() == 0 || index >= lower.length()) {
                    break;
                }
                char unit = lower.charAt(index++);
                long amount;
                try {
                    amount = Long.parseLong(digits.toString());
                } catch (NumberFormatException e) {
                    break;
                }
                switch (unit) {
                    case 's':
                        total += amount * 1_000L;
                        break;
                    case 'm':
                        total += amount * 60_000L;
                        break;
                    case 'h':
                        total += amount * 3_600_000L;
                        break;
                    case 'd':
                        total += amount * 86_400_000L;
                        break;
                    case 'w':
                        total += amount * 604_800_000L;
                        break;
                    default:
                        break;
                }
            }
            return total;
        }

        public Builder permanent() {
            this.expiresAt = -1L;
            return this;
        }

        public PunishmentRequest build() {
            return new PunishmentRequest(this);
        }

    }

}
