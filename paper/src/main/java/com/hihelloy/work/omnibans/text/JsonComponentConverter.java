package com.hihelloy.work.omnibans.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.Map;

final class JsonComponentConverter {

    private JsonComponentConverter() {
    }

    @SuppressWarnings("unchecked")
    static Component convert(Object node) {
        if (node instanceof List<?> list) {
            return convertList(list);
        }
        if (node instanceof String string) {
            return Component.text(string);
        }
        if (node instanceof Map<?, ?> map) {
            return convertObject((Map<String, Object>) map);
        }
        return Component.empty();
    }

    private static Component convertList(List<?> list) {
        Component result = Component.empty();
        for (Object item : list) {
            result = result.append(convert(item));
        }
        return result;
    }

    private static Component convertObject(Map<String, Object> map) {
        String text = asString(map.get("text"));
        Component component = Component.text(text != null ? text : "");
        component = applyColor(component, asString(map.get("color")));
        component = applyDecoration(component, map, "bold", TextDecoration.BOLD);
        component = applyDecoration(component, map, "italic", TextDecoration.ITALIC);
        component = applyDecoration(component, map, "underlined", TextDecoration.UNDERLINED);
        component = applyDecoration(component, map, "strikethrough", TextDecoration.STRIKETHROUGH);
        component = applyDecoration(component, map, "obfuscated", TextDecoration.OBFUSCATED);
        Object extra = map.get("extra");
        if (extra instanceof List<?> extraList) {
            for (Object child : extraList) {
                component = component.append(convert(child));
            }
        }
        return component;
    }

    private static Component applyColor(Component component, String colorString) {
        if (colorString == null) {
            return component;
        }
        if (colorString.startsWith("#")) {
            TextColor textColor = TextColor.fromHexString(colorString);
            if (textColor != null) {
                return component.color(textColor);
            }
            return component;
        }
        NamedTextColor namedTextColor = NamedTextColor.NAMES.value(colorString.toLowerCase());
        if (namedTextColor != null) {
            return component.color(namedTextColor);
        }
        return component;
    }

    private static Component applyDecoration(Component component, Map<String, Object> map, String key, TextDecoration decoration) {
        Object value = map.get(key);
        if (Boolean.TRUE.equals(value)) {
            return component.decorate(decoration);
        }
        if (Boolean.FALSE.equals(value)) {
            return component.decoration(decoration, TextDecoration.State.FALSE);
        }
        return component;
    }

    private static String asString(Object value) {
        if (value instanceof String string) {
            return string;
        }
        return null;
    }

}
