package com.hihelloy.work.omnibans.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class MessageFormatter {

    private static final Pattern VANILLA_HEX_PATTERN = Pattern.compile("[&§]x((?:[&§][0-9a-fA-F]){6})");
    private static final Pattern COMPACT_HEX_PATTERN = Pattern.compile("[&§]#([0-9a-fA-F]{6})");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[&§]([0-9a-fA-Fk-oK-OrR])");
    private static final Map<String, String> LEGACY_TAG_MAP = buildLegacyTagMap();

    private final MiniMessage miniMessage;

    public MessageFormatter() {
        this.miniMessage = MiniMessage.miniMessage();
    }

    public Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        String trimmed = input.trim();
        if (looksLikeJson(trimmed)) {
            Component jsonResult = tryParseJson(trimmed);
            if (jsonResult != null) {
                return jsonResult;
            }
        }
        return parseText(input);
    }

    private boolean looksLikeJson(String trimmed) {
        boolean object = trimmed.startsWith("{") && trimmed.endsWith("}");
        boolean array = trimmed.startsWith("[") && trimmed.endsWith("]");
        return object || array;
    }

    private Component tryParseJson(String trimmed) {
        try {
            Object node = SimpleJsonParser.parse(trimmed);
            return JsonComponentConverter.convert(node);
        } catch (Exception exception) {
            return null;
        }
    }

    private Component parseText(String input) {
        String normalized = normalizeLegacyCodes(input);
        return miniMessage.deserialize(normalized);
    }

    private String normalizeLegacyCodes(String input) {
        String afterVanillaHex = VANILLA_HEX_PATTERN.matcher(input).replaceAll(MessageFormatter::replaceVanillaHex);
        String afterCompactHex = COMPACT_HEX_PATTERN.matcher(afterVanillaHex).replaceAll(MessageFormatter::replaceCompactHex);
        return LEGACY_CODE_PATTERN.matcher(afterCompactHex).replaceAll(MessageFormatter::replaceLegacyCode);
    }

    private static String replaceVanillaHex(MatchResult matchResult) {
        String digits = matchResult.group(1).replaceAll("[&§]", "").toLowerCase();
        return "<#" + digits + ">";
    }

    private static String replaceCompactHex(MatchResult matchResult) {
        return "<#" + matchResult.group(1).toLowerCase() + ">";
    }

    private static String replaceLegacyCode(MatchResult matchResult) {
        String code = matchResult.group(1).toLowerCase();
        String tag = LEGACY_TAG_MAP.get(code);
        if (tag == null) {
            return matchResult.group();
        }
        return "<" + tag + ">";
    }

    private static Map<String, String> buildLegacyTagMap() {
        Map<String, String> map = new HashMap<>();
        map.put("0", "black");
        map.put("1", "dark_blue");
        map.put("2", "dark_green");
        map.put("3", "dark_aqua");
        map.put("4", "dark_red");
        map.put("5", "dark_purple");
        map.put("6", "gold");
        map.put("7", "gray");
        map.put("8", "dark_gray");
        map.put("9", "blue");
        map.put("a", "green");
        map.put("b", "aqua");
        map.put("c", "red");
        map.put("d", "light_purple");
        map.put("e", "yellow");
        map.put("f", "white");
        map.put("k", "obfuscated");
        map.put("l", "bold");
        map.put("m", "strikethrough");
        map.put("n", "underlined");
        map.put("o", "italic");
        map.put("r", "reset");
        return map;
    }

}
