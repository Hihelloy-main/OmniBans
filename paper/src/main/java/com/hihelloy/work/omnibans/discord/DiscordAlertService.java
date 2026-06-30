package com.hihelloy.work.omnibans.discord;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.util.PaperLoggerAdapter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DiscordAlertService {

    private final OmniBans plugin;
    private DiscordBotClient client;

    public DiscordAlertService(OmniBans plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        if (plugin.getOmniBansConfig().isDiscordBotEnabled()) {
            client = new DiscordBotClient(
                plugin.getOmniBansConfig().getDiscordBotToken(),
                plugin.getOmniBansConfig().getDiscordChannelId(),
                new PaperLoggerAdapter(plugin.getLogger()));
        } else {
            client = null;
        }
    }

    public void announcePunishment(String title, int color, UUID targetUuid, String targetName, String staffName, String reason) {
        if (client == null || targetUuid == null) {
            return;
        }
        plugin.getAltLookupService().findAlts(targetUuid).thenAccept(altMap ->
            plugin.getAltLookupService().findBannedAltNames(altMap).thenAccept(bannedAlts ->
                send(title, color, targetName, staffName, reason, altMap, bannedAlts)));
    }

    public void announceAltJoin(String targetName, Map<UUID, String> altMap, List<String> bannedAlts) {
        if (client == null) {
            return;
        }
        send("Possible alt account joined", 0xFFA500, targetName, null, null, altMap, bannedAlts);
    }

    private void send(String title, int color, String targetName, String staffName, String reason, Map<UUID, String> altMap, List<String> bannedAlts) {
        DiscordEmbedBuilder builder = new DiscordEmbedBuilder()
            .title(title)
            .color(color)
            .pingRole(plugin.getOmniBansConfig().getDiscordAdminRoleId())
            .field("Player", targetName);
        if (staffName != null) {
            builder.field("Staff", staffName);
        }
        if (reason != null) {
            builder.field("Reason", reason);
        }
        builder.field("Known alts", altMap.isEmpty() ? "None" : String.join(", ", altMap.values()));
        builder.field("Banned alts", bannedAlts.isEmpty() ? "None" : String.join(", ", bannedAlts));
        client.sendEmbed(builder.build());
    }

}
