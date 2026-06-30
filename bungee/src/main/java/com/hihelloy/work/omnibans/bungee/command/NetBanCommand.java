package com.hihelloy.work.omnibans.bungee.command;

import com.hihelloy.work.omnibans.bungee.OmniBansBungee;
import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.net.InetSocketAddress;
import java.util.Arrays;
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
            sender.sendMessage(legacy("&cYou do not have permission to do that."));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(legacy("&cUsage: /netban <player> [reason]"));
            return;
        }
        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(legacy("&cThat player is not currently online."));
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
        target.disconnect(legacy("&c&lYou have been banned from this network"));
        if (plugin.getNetworkMessenger().isActive()) {
            NetworkPacket packet = new NetworkPacket(NetworkAction.PUNISHMENT_ADDED, inserted.getId(), inserted.getType().name(), inserted.getTargetUuid(), inserted.getTargetName(), plugin.getBungeeConfig().getServerName());
            plugin.getNetworkMessenger().publish(packet);
        }
        sender.sendMessage(legacy("&7You have &cbanned &f" + inserted.getTargetName()));
    }

    private BaseComponent[] legacy(String message) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
    }

}
