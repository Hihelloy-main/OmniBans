package com.hihelloy.work.omnibans.command;

import com.hihelloy.work.omnibans.OmniBans;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;

import java.lang.reflect.Field;
import java.util.List;

public final class CommandOverrideService {

    private static final List<String> OWNED_LABELS = List.of(
        "ban", "tempban", "tban", "unban", "banip", "ban-ip", "ipban", "ip-ban", "unbanip",
        "mute", "silence", "tempmute", "tmute", "unmute", "kick", "warn",
        "banlist", "mutelist", "pardon", "pardon-ip");

    private final OmniBans plugin;

    public CommandOverrideService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void applyIfNeeded() {
        boolean essentialsPresent = Bukkit.getPluginManager().getPlugin("Essentials") != null;
        if (!essentialsPresent) {
            return;
        }
        plugin.getLogger().info("Essentials detected, OmniBans is taking over its moderation commands.");
        CommandMap commandMap = resolveCommandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("Could not access the server's command map, Essentials override was skipped.");
            return;
        }
        for (String label : OWNED_LABELS) {
            PluginCommand command = plugin.getCommand(label);
            if (command == null) {
                continue;
            }
            commandMap.register(label, "omnibans", command);
        }
    }

    private CommandMap resolveCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception exception) {
            return null;
        }
    }

}
