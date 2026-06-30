package com.hihelloy.work.omnibans.velocity.command;

import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.velocity.OmniBansVelocity;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.UUID;

public final class NetMuteCommand implements SimpleCommand {

    private final OmniBansVelocity plugin;
    private final MiniMessage miniMessage;

    public NetMuteCommand(OmniBansVelocity plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(Invocation invocation) {
        com.velocitypowered.api.command.CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (!source.hasPermission("omnibans.netmute")) {
            source.sendMessage(prefixed("<red>You do not have permission to do that."));
            return;
        }
        if (args.length < 1) {
            source.sendMessage(prefixed("<red>Usage: /netmute <player> [reason]"));
            return;
        }
        Player target = plugin.getProxyServer().getPlayer(args[0]).orElse(null);
        if (target == null) {
            source.sendMessage(prefixed("<red>That player is not currently online."));
            return;
        }
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason specified";
        UUID staffUuid = source instanceof Player player ? player.getUniqueId() : null;
        String staffName = source instanceof Player player ? player.getUsername() : "Console";
        Punishment punishment = Punishment.builder()
            .type(PunishmentType.MUTE)
            .scope(PunishmentScope.GLOBAL)
            .server(plugin.getVelocityConfig().getServerName())
            .targetUuid(target.getUniqueId())
            .targetName(target.getUsername())
            .staffUuid(staffUuid)
            .staffName(staffName)
            .reason(reason)
            .expiresAt(-1L)
            .build();
        plugin.getStorage().insert(punishment).thenAccept(inserted -> finish(source, target, inserted, reason));
    }

    private void finish(com.velocitypowered.api.command.CommandSource source, Player target, Punishment inserted, String reason) {
        if (plugin.getNetworkMessenger().isActive()) {
            NetworkPacket packet = new NetworkPacket(NetworkAction.PUNISHMENT_ADDED, inserted.getId(), inserted.getType().name(), inserted.getTargetUuid(), inserted.getTargetName(), plugin.getVelocityConfig().getServerName());
            plugin.getNetworkMessenger().publish(packet);
        }
        target.sendMessage(prefixed("<gray>You have been muted. Reason: <white>" + reason));
        source.sendMessage(prefixed("<gray>You have <red>muted <white>" + inserted.getTargetName()));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("omnibans.netmute");
    }

    private Component prefixed(String message) {
        return miniMessage.deserialize(plugin.getVelocityConfig().getPrefix() + " " + message);
    }

}
