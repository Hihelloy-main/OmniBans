package com.hihelloy.work.omnibans.gui;

public final class ConfigField {

    private final String path;
    private final FieldType type;
    private final String displayValue;

    public ConfigField(String path, FieldType type, String displayValue) {
        this.path = path;
        this.type = type;
        this.displayValue = displayValue;
    }

    public String getPath() {
        return path;
    }

    public FieldType getType() {
        return type;
    }

    public String getDisplayValue() {
        return displayValue;
    }

}