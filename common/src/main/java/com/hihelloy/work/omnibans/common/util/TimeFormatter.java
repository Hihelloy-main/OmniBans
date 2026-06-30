package com.hihelloy.work.omnibans.common.util;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String format(long millis) {
        if (millis <= 0) {
            return "0s";
        }
        long seconds = millis / 1000L;
        long days = seconds / 86400L;
        seconds %= 86400L;
        long hours = seconds / 3600L;
        seconds %= 3600L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || builder.length() == 0) {
            builder.append(seconds).append("s");
        }
        return builder.toString().trim();
    }

    public static String formatRemaining(long expiresAt) {
        if (expiresAt <= 0) {
            return "Permanent";
        }
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining <= 0) {
            return "Expired";
        }
        return format(remaining);
    }

}
