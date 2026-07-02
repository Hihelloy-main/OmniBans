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

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NetBanCommand extends Command {

    private final OmniBansBungee plugin;

    public NetBanCommand(OmniBansBungee plugin) {
        super("netban", "omnibans.netban");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("omnibans.netban")) {
            sender.sendMessage(plugin.getMessages().components("netban.no-permission"));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessages().components("netban.usage"));
            return;
        }
        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessages().components("netban.not-online"));
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason specified";
        UUID staffUuid = sender instanceof ProxiedPlayer player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        String ip = ((InetSocketAddress) target.getSocketAddress()).getAddress().getHostAddress();
        Punishment punishment = Punishment.builder()
                .type(PunishmentType.BAN)
                .scope(PunishmentScope.GLOBAL)
                .server(plugin.getBungeeConfig().getServerName())
                .targetUuid(target.getUniqueId())
                .targetName(target.getName())
                .targetIp(ip)
                .staffUuid(staffUuid)
                .staffName(staffName)
                .reason(reason)
                .expiresAt(-1L)
                .build();
        plugin.getStorage().insert(punishment).thenAccept(inserted -> finish(sender, target, inserted));
    }

    private void finish(CommandSender sender, ProxiedPlayer target, Punishment inserted) {
        target.disconnect(plugin.getMessages().components("ban.network-kick"));
        if (plugin.getNetworkMessenger().isActive()) {
            NetworkPacket packet = new NetworkPacket(NetworkAction.PUNISHMENT_ADDED, inserted.getId(), inserted.getType().name(), inserted.getTargetUuid(), inserted.getTargetName(), plugin.getBungeeConfig().getServerName());
            plugin.getNetworkMessenger().publish(packet);
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", inserted.getTargetName());
        sender.sendMessage(plugin.getMessages().components("netban.success", placeholders));
    }

}