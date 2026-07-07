package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class UnbanIpCommand extends AbstractSubCommand {

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    public UnbanIpCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "unbanip";
    }

    @Override
    public String permission() {
        return "omnibans.unbanip";
    }

    @Override
    public String usage() {
        return "/unbanip <ip|player>";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        String input = args[0];
        if (IP_PATTERN.matcher(input).matches()) {
            plugin.getPunishmentService().unbanIp(input, staffUuid, staffName, "Unbanned").thenAccept(success ->
                send(sender, success ? "unbanip.raw-success" : "unbanip.not-banned-raw", Map.of()));
            return;
        }
        plugin.getPlayerResolver().resolve(input).thenAccept(resolved ->
            plugin.getStorage().findKnownIps(resolved.getUuid()).thenAccept(ips -> {
                if (ips.isEmpty()) {
                    send(sender, "banip.no-ip-found", Map.of("target", resolved.getName()));
                    return;
                }
                tryEachIp(sender, resolved.getName(), ips, 0, staffUuid, staffName);
            }));
    }

    private void tryEachIp(CommandSender sender, String targetName, List<String> ips, int index, UUID staffUuid, String staffName) {
        if (index >= ips.size()) {
            send(sender, "unbanip.not-banned", Map.of("target", targetName));
            return;
        }
        String ip = ips.get(index);
        plugin.getPunishmentService().unbanIp(ip, staffUuid, staffName, "Unbanned").thenAccept(success -> {
            if (success) {
                send(sender, "unbanip.success", Map.of("target", targetName));
            } else {
                tryEachIp(sender, targetName, ips, index + 1, staffUuid, staffName);
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(knownPlayerNames(), args[0]);
        }
        return Collections.emptyList();
    }

}
