package com.hihelloy.work.omnibans.common.impl;

import com.hihelloy.work.omnibans.api.model.ApiPunishment;
import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;
import com.hihelloy.work.omnibans.common.punishment.Punishment;

import java.util.UUID;

public final class CommonApiPunishment implements ApiPunishment {

    private final Punishment punishment;

    public CommonApiPunishment(Punishment punishment) {
        this.punishment = punishment;
    }

    public Punishment unwrap() {
        return punishment;
    }

    @Override
    public long getId() {
        return punishment.getId();
    }

    @Override
    public ApiPunishmentType getType() {
        return ApiPunishmentType.valueOf(punishment.getType().name());
    }

    @Override
    public UUID getTargetUuid() {
        return punishment.getTargetUuid();
    }

    @Override
    public String getTargetName() {
        return punishment.getTargetName();
    }

    @Override
    public String getTargetIp() {
        return punishment.getTargetIp();
    }

    @Override
    public UUID getStaffUuid() {
        return punishment.getStaffUuid();
    }

    @Override
    public String getStaffName() {
        return punishment.getStaffName();
    }

    @Override
    public String getReason() {
        return punishment.getReason();
    }

    @Override
    public long getCreatedAt() {
        return punishment.getCreatedAt();
    }

    @Override
    public long getExpiresAt() {
        return punishment.getExpiresAt();
    }

    @Override
    public boolean isPermanent() {
        return punishment.isPermanent();
    }

    @Override
    public boolean isExpired() {
        return punishment.isExpired();
    }

    @Override
    public boolean isActive() {
        return punishment.isActive();
    }

}
