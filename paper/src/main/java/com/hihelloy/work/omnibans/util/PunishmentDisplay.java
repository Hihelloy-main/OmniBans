package com.hihelloy.work.omnibans.util;

import com.hihelloy.work.omnibans.common.punishment.Punishment;

import java.util.regex.Pattern;

public final class PunishmentDisplay {

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    private PunishmentDisplay() {
    }

    public static String safeName(Punishment punishment) {
        return safeName(punishment.getTargetName());
    }

    public static String safeName(String targetName) {
        if (targetName == null) {
            return "An unnamed ip address";
        }
        if (IP_PATTERN.matcher(targetName).matches()) {
            return "An ip address";
        }
        return targetName;
    }

}
