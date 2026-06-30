package com.hihelloy.work.omnibans.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {

    private static final Pattern PATTERN = Pattern.compile("(\\d+)([smhdwy])");

    private DurationParser() {
    }

    public static long parse(String input) {
        if (input == null) {
            return -1L;
        }
        String trimmed = input.trim().toLowerCase();
        if (trimmed.equals("perm") || trimmed.equals("permanent") || trimmed.equals("-1")) {
            return -1L;
        }
        Matcher matcher = PATTERN.matcher(trimmed);
        long totalMillis = 0L;
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            totalMillis += amount * unitMillis(unit);
        }
        if (!matched) {
            return 0L;
        }
        return System.currentTimeMillis() + totalMillis;
    }

    private static long unitMillis(String unit) {
        switch (unit) {
            case "s":
                return 1000L;
            case "m":
                return 60000L;
            case "h":
                return 3600000L;
            case "d":
                return 86400000L;
            case "w":
                return 604800000L;
            case "y":
                return 31536000000L;
            default:
                return 0L;
        }
    }

}
