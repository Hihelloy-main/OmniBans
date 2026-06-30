package com.hihelloy.work.omnibans.task;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;

public final class ExpiryTask {

    private final OmniBans plugin;
    private volatile boolean running;

    public ExpiryTask(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void start() {
        running = true;
        plugin.getScheduler().runGlobalRepeating(this::sweep, 1200L, 1200L);
    }

    public void stop() {
        running = false;
    }

    private void sweep() {
        if (!running) {
            return;
        }
        plugin.getStorage().sweepExpired().thenAccept(expired -> {
            for (Punishment punishment : expired) {
                uncache(punishment);
            }
        });
    }

    private void uncache(Punishment punishment) {
        if (punishment.getType() == PunishmentType.BAN && punishment.getTargetUuid() != null) {
            plugin.getCache().uncacheBan(punishment.getTargetUuid());
        }
        if (punishment.getType() == PunishmentType.IP_BAN && punishment.getTargetIp() != null) {
            plugin.getCache().uncacheIpBan(punishment.getTargetIp());
        }
        if (punishment.getType() == PunishmentType.MUTE && punishment.getTargetUuid() != null) {
            plugin.getCache().uncacheMute(punishment.getTargetUuid());
        }
        if (punishment.getType() == PunishmentType.IP_MUTE && punishment.getTargetIp() != null) {
            plugin.getCache().uncacheIpMute(punishment.getTargetIp());
        }
    }

}
