package com.hihelloy.work.omnibans.api;

import com.hihelloy.work.omnibans.api.event.PostPunishmentEvent;
import com.hihelloy.work.omnibans.api.model.ApiPunishment;
import com.hihelloy.work.omnibans.api.model.ApiPunishmentType;
import com.hihelloy.work.omnibans.common.impl.CommonApiPunishment;

import java.util.UUID;

final class PostPunishmentEventImpl implements PostPunishmentEvent {

    private final CommonApiPunishment punishment;

    PostPunishmentEventImpl(CommonApiPunishment punishment) {
        this.punishment = punishment;
    }

    @Override
    public ApiPunishment getPunishment() {
        return punishment;
    }

    @Override
    public ApiPunishmentType getPunishmentType() {
        return punishment.getType();
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

}
