package com.hihelloy.work.omnibans.bungee.command;

import com.hihelloy.work.omnibans.bungee.OmniBansBungee;
import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NetMuteCommand extends Command {

    private final OmniBansBungee plugin;

    public NetMuteCommand(OmniBansBungee plugin) {
        super("netmute", "omnibans.netmute");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("omnibans.netmute")) {
            sender.sendMessage(plugin.getMessages().components("netmute.no-permission"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessages().components("netmute.usage"));
            return;
        }
        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessages().components("netmute.not-online"));
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason specified";
        UUID staffUuid = sender instanceof ProxiedPlayer player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        Punishment punishment = Punishment.builder()
                .type(PunishmentType.MUTE)
                .scope(PunishmentScope.GLOBAL)
                .server(plugin.getBungeeConfig().getServerName())
                .targetUuid(target.getUniqueId())
                .targetName(target.getName())
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(reason)
                .expiresAt(-1L)
                .build();
        plugin.getStorage().insert(punishment).thenAccept(inserted -> finish(sender, target, inserted, reason));
    }

    private void finish(CommandSender sender, ProxiedPlayer target, Punishment inserted, String reason) {
        if (plugin.getNetworkMessenger().isActive()) {
            NetworkPacket packet = new NetworkPacket(NetworkAction.PUNISHMENT_ADDED, inserted.getId(), inserted.getType().name(), inserted.getTargetUuid(), inserted.getTargetName(), plugin.getBungeeConfig().getServerName());
            plugin.getNetworkMessenger().publish(packet);
        }
        Map<String, String> notifyPlaceholders = new HashMap<>();
        notifyPlaceholders.put("reason", reason);
        target.sendMessage(plugin.getMessages().components("netmute.notify", notifyPlaceholders));
        Map<String, String> successPlaceholders = new HashMap<>();
        successPlaceholders.put("target", inserted.getTargetName());
        sender.sendMessage(plugin.getMessages().components("netmute.success", successPlaceholders));
    }

}