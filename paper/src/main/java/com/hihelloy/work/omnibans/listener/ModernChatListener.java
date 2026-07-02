package com.hihelloy.work.omnibans.listener;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.util.TimeFormatter;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public final class ModernChatListener implements Listener {

    private final OmniBans plugin;

    public ModernChatListener(OmniBans plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (plugin.getConfigGuiService().tryHandleChatInput(event.getPlayer(), plainText)) {
            event.setCancelled(true);
            return;
        }
        Punishment mute = plugin.getCache().getMute(event.getPlayer().getUniqueId());
        if (mute == null) {
            return;
        }
        if (mute.isExpired()) {
            plugin.getCache().uncacheMute(event.getPlayer().getUniqueId());
            return;
        }
        event.setCancelled(true);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", mute.getReason() != null ? mute.getReason() : "No reason specified");
        placeholders.put("expires", mute.isPermanent() ? "Never" : TimeFormatter.formatRemaining(mute.getExpiresAt()));
        Component component = plugin.getMessages().component("mute.blocked", placeholders);
        plugin.getMessageDispatcher().send(event.getPlayer(), component);
        plugin.getStaffAlertService().alertMutedChat(event.getPlayer().getName(), plainText);
    }

}