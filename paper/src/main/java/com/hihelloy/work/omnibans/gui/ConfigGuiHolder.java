package com.hihelloy.work.omnibans.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public final class ConfigGuiHolder implements InventoryHolder {

    private final String target;
    private final List<ConfigField> fields;
    private final int page;
    private Inventory inventory;

    public ConfigGuiHolder(String target, List<ConfigField> fields, int page) {
        this.target = target;
        this.fields = fields;
        this.page = page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getTarget() {
        return target;
    }

    public List<ConfigField> getFields() {
        return fields;
    }

    public int getPage() {
        return page;
    }

}
