package com.hihelloy.work.omnibans.velocity.command;

import com.hihelloy.work.omnibans.common.network.NetworkAction;
import com.hihelloy.work.omnibans.common.network.NetworkPacket;
import com.hihelloy.work.omnibans.common.punishment.Punishment;
import com.hihelloy.work.omnibans.common.punishment.PunishmentScope;
import com.hihelloy.work.omnibans.common.punishment.PunishmentType;
import com.hihelloy.work.omnibans.velocity.OmniBansVelocity;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NetMuteCommand implements SimpleCommand {

    private final OmniBansVelocity plugin;

    public NetMuteCommand(OmniBansVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        com.velocitypowered.api.command.CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (!source.hasPermission("omnibans.netmute")) {
            source.sendMessage(plugin.getMessages().component("netmute.no-permission"));
            return;
        }
        if (args.length < 1) {
            source.sendMessage(plugin.getMessages().component("netmute.usage"));
            return;
        }
        Player target = plugin.getProxyServer().getPlayer(args[0]).orElse(null);
        if (target == null) {
            source.sendMessage(plugin.getMessages().component("netmute.not-online"));
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
        Map<String, String> reasonPlaceholders = new HashMap<>();
        reasonPlaceholders.put("reason", reason);
        target.sendMessage(plugin.getMessages().component("netmute.notify", reasonPlaceholders));
        Map<String, String> successPlaceholders = new HashMap<>();
        successPlaceholders.put("target", inserted.getTargetName());
        source.sendMessage(plugin.getMessages().component("netmute.success", successPlaceholders));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("omnibans.netmute");
    }

}
