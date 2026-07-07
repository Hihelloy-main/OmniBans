package com.hihelloy.work.omnibans.gui;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class YamlFlattener {

    private YamlFlattener() {
    }

    public static List<ConfigField> flatten(ConfigurationSection section) {
        List<ConfigField> fields = new ArrayList<>();
        collect(section, fields);
        Collections.sort(fields, (first, second) -> first.getPath().compareToIgnoreCase(second.getPath()));
        return fields;
    }

    private static void collect(ConfigurationSection section, List<ConfigField> fields) {
        for (String key : section.getKeys(false)) {
            String path = section.getCurrentPath().isEmpty() ? key : section.getCurrentPath() + "." + key;
            if (section.isConfigurationSection(key)) {
                collect(section.getConfigurationSection(key), fields);
                continue;
            }
            Object value = section.get(key);
            fields.add(toField(path, value));
        }
    }

    private static ConfigField toField(String path, Object value) {
        if (value instanceof Boolean) {
            return new ConfigField(path, FieldType.BOOLEAN, value.toString());
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float) {
            return new ConfigField(path, FieldType.NUMBER, value.toString());
        }
        if (value instanceof List) {
            return new ConfigField(path, FieldType.LIST, joinList((List<?>) value));
        }
        return new ConfigField(path, FieldType.STRING, value != null ? value.toString() : "");
    }

    private static String joinList(List<?> list) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < list.size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(list.get(index));
        }
        return builder.toString();
    }

}
