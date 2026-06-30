package com.hihelloy.work.omnibans.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageDispatcher {

    private final boolean adventureNative;
    private final LegacyComponentSerializer legacySerializer;

    public MessageDispatcher() {
        this.adventureNative = checkAdventureSupport();
        this.legacySerializer = LegacyComponentSerializer.builder().character('§').hexColors().build();
    }

    private boolean checkAdventureSupport() {
        try {
            CommandSender.class.getMethod("sendMessage", Component.class);
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }

    public void send(CommandSender sender, Component component) {
        if (adventureNative) {
            sender.sendMessage(component);
            return;
        }
        if (sender instanceof Player player) {
            BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(component);
            player.spigot().sendMessage(baseComponents);
            return;
        }
        sender.sendMessage(legacySerializer.serialize(component));
    }

    @SuppressWarnings("deprecation")
    public void kick(Player player, Component component) {
        if (adventureNative) {
            player.kick(component);
            return;
        }
        player.kickPlayer(legacySerializer.serialize(component));
    }

    public String flatten(Component component) {
        return legacySerializer.serialize(component);
    }

}
