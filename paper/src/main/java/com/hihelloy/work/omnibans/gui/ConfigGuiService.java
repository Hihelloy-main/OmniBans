package com.hihelloy.work.omnibans.gui;

import com.hihelloy.work.omnibans.OmniBans;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigGuiService {

    private static final int PAGE_SIZE = 45;
    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_CLOSE = 49;
    private static final int SLOT_NEXT = 53;
    private static final long EDIT_TIMEOUT_MILLIS = 60000L;

    private final OmniBans plugin;
    private final Map<UUID, PendingEdit> pendingEdits = new ConcurrentHashMap<>();

    public ConfigGuiService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void openConfig(Player player, int page) {
        open(player, "config", page);
    }

    public void openMessages(Player player, int page) {
        open(player, "messages", page);
    }

    private void open(Player player, String target, int page) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileFor(target));
        List<ConfigField> fields = YamlFlattener.flatten(yaml);
        int totalPages = Math.max(1, (int) Math.ceil(fields.size() / (double) PAGE_SIZE));
        int boundedPage = Math.max(0, Math.min(page, totalPages - 1));
        ConfigGuiHolder holder = new ConfigGuiHolder(target, fields, boundedPage);
        String title = ChatColor.translateAlternateColorCodes('&', "&8OmniBans " + target + ".yml &7(" + (boundedPage + 1) + "/" + totalPages + ")");
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);
        renderPage(inventory, holder);
        player.openInventory(inventory);
    }

    private void renderPage(Inventory inventory, ConfigGuiHolder holder) {
        List<ConfigField> fields = holder.getFields();
        int start = holder.getPage() * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, fields.size());
        for (int index = start; index < end; index++) {
            inventory.setItem(index - start, buildFieldItem(fields.get(index)));
        }
        if (holder.getPage() > 0) {
            inventory.setItem(SLOT_PREVIOUS, buildButton(Material.ARROW, "&aPrevious page"));
        }
        inventory.setItem(SLOT_CLOSE, buildButton(Material.BARRIER, "&cClose"));
        if (end < fields.size()) {
            inventory.setItem(SLOT_NEXT, buildButton(Material.ARROW, "&aNext page"));
        }
    }

    public void handleClick(Player player, ConfigGuiHolder holder, int slot) {
        if (slot == SLOT_PREVIOUS) {
            open(player, holder.getTarget(), holder.getPage() - 1);
            return;
        }
        if (slot == SLOT_NEXT) {
            open(player, holder.getTarget(), holder.getPage() + 1);
            return;
        }
        if (slot == SLOT_CLOSE) {
            player.closeInventory();
            return;
        }
        int index = holder.getPage() * PAGE_SIZE + slot;
        if (slot >= PAGE_SIZE || index >= holder.getFields().size()) {
            return;
        }
        ConfigField field = holder.getFields().get(index);
        if (field.getType() == FieldType.BOOLEAN) {
            toggleBoolean(player, holder, slot, index, field);
            return;
        }
        promptChatInput(player, holder.getTarget(), field);
        player.closeInventory();
    }

    private void toggleBoolean(Player player, ConfigGuiHolder holder, int slot, int index, ConfigField field) {
        boolean newValue = !Boolean.parseBoolean(field.getDisplayValue());
        if (!writeValue(holder.getTarget(), field.getPath(), newValue)) {
            player.sendMessage(ChatColor.RED + "Failed to save that change, check the console for details.");
            return;
        }
        ConfigField updated = new ConfigField(field.getPath(), FieldType.BOOLEAN, String.valueOf(newValue));
        holder.getFields().set(index, updated);
        holder.getInventory().setItem(slot, buildFieldItem(updated));
        player.sendMessage(ChatColor.GREEN + "Set " + ChatColor.WHITE + field.getPath() + ChatColor.GREEN + " to " + ChatColor.WHITE + newValue);
    }

    private void promptChatInput(Player player, String target, ConfigField field) {
        pendingEdits.put(player.getUniqueId(), new PendingEdit(target, field.getPath(), field.getType(), System.currentTimeMillis() + EDIT_TIMEOUT_MILLIS));
        player.sendMessage(ChatColor.YELLOW + "Type the new value for " + ChatColor.WHITE + field.getPath() + ChatColor.YELLOW + " in chat, or type 'cancel'. Use \\n for a line break. You have 60 seconds.");
    }

    public boolean tryHandleChatInput(Player player, String rawMessage) {
        PendingEdit pending = pendingEdits.get(player.getUniqueId());
        if (pending == null) {
            return false;
        }
        if (System.currentTimeMillis() > pending.expiresAt) {
            pendingEdits.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "That edit prompt timed out.");
            return true;
        }
        if (rawMessage.equalsIgnoreCase("cancel")) {
            pendingEdits.remove(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Edit cancelled.");
            return true;
        }
        Object coerced = coerce(pending.type, rawMessage);
        if (coerced == null) {
            player.sendMessage(ChatColor.RED + "That is not a valid number, try again or type 'cancel'.");
            return true;
        }
        if (!writeValue(pending.target, pending.path, coerced)) {
            player.sendMessage(ChatColor.RED + "Failed to save that change, check the console for details.");
            pendingEdits.remove(player.getUniqueId());
            return true;
        }
        pendingEdits.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Set " + ChatColor.WHITE + pending.path + ChatColor.GREEN + " to " + ChatColor.WHITE + coerced);
        return true;
    }

    public void clearPending(Player player) {
        pendingEdits.remove(player.getUniqueId());
    }

    private Object coerce(FieldType type, String rawMessage) {
        if (type == FieldType.LIST) {
            String[] parts = rawMessage.split(",");
            List<String> values = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
            return values;
        }
        if (type == FieldType.NUMBER) {
            try {
                return Long.parseLong(rawMessage.trim());
            } catch (NumberFormatException firstAttempt) {
                try {
                    return Double.parseDouble(rawMessage.trim());
                } catch (NumberFormatException secondAttempt) {
                    return null;
                }
            }
        }
        return rawMessage.replace("\\n", "\n");
    }

    private boolean writeValue(String target, String path, Object value) {
        File file = fileFor(target);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        yaml.set(path, value);
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save " + target + ".yml: " + exception.getMessage());
            return false;
        }
        if (target.equals("config")) {
            plugin.getOmniBansConfig().load();
        } else {
            plugin.getMessages().load();
        }
        return true;
    }

    private File fileFor(String target) {
        return new File(plugin.getDataFolder(), target + ".yml");
    }

    private ItemStack buildFieldItem(ConfigField field) {
        ItemStack item = new ItemStack(materialFor(field));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + field.getPath());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Current value:");
        lore.addAll(wrap(field.getDisplayValue()));
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + instructionFor(field.getType()));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return item;
    }

    private Material materialFor(ConfigField field) {
        if (field.getType() == FieldType.BOOLEAN) {
            return Boolean.parseBoolean(field.getDisplayValue()) ? Material.EMERALD : Material.BARRIER;
        }
        if (field.getType() == FieldType.LIST) {
            return Material.BOOK;
        }
        return Material.PAPER;
    }

    private String instructionFor(FieldType type) {
        if (type == FieldType.BOOLEAN) {
            return "Click to toggle";
        }
        return "Click to enter a new value in chat";
    }

    private List<String> wrap(String value) {
        List<String> lines = new ArrayList<>();
        for (String rawLine : value.split("\n")) {
            if (rawLine.length() <= 40) {
                lines.add(ChatColor.WHITE + rawLine);
                continue;
            }
            for (String chunk : chunk(rawLine, 40)) {
                lines.add(ChatColor.WHITE + chunk);
            }
        }
        if (lines.isEmpty()) {
            lines.add(ChatColor.WHITE + "(empty)");
        }
        return lines;
    }

    private List<String> chunk(String text, int size) {
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < text.length(); start += size) {
            chunks.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return chunks;
    }

    private static final class PendingEdit {

        private final String target;
        private final String path;
        private final FieldType type;
        private final long expiresAt;

        private PendingEdit(String target, String path, FieldType type, long expiresAt) {
            this.target = target;
            this.path = path;
            this.type = type;
            this.expiresAt = expiresAt;
        }

    }

}
