package com.hihelloy.work.omnibans.command.impl;

import com.hihelloy.work.omnibans.OmniBans;
import com.hihelloy.work.omnibans.command.AbstractSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class BanIpCommand extends AbstractSubCommand {

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

    public BanIpCommand(OmniBans plugin) {
        super(plugin);
    }

    @Override
    public String name() {
        return "banip";
    }

    @Override
    public String permission() {
        return "omnibans.banip";
    }

    @Override
    public String usage() {
        return "/banip <ip|player> [reason]";
    }

    @Override
    protected void run(CommandSender sender, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return;
        }
        String input = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason specified";
        UUID staffUuid = sender instanceof Player player ? player.getUniqueId() : null;
        String staffName = sender.getName();
        if (IP_PATTERN.matcher(input).matches()) {
            applyBan(sender, input, input, staffUuid, staffName, reason);
            return;
        }
        Player online = plugin.getServer().getPlayerExact(input);
        if (online != null && online.getAddress() != null) {
            String ip = online.getAddress().getAddress().getHostAddress();
            guardedApply(sender, online.getUniqueId(), input, ip, staffUuid, staffName, reason);
            return;
        }
        plugin.getPlayerResolver().resolve(input).thenAccept(resolved ->
            plugin.getStorage().findKnownIps(resolved.getUuid()).thenAccept(ips -> {
                if (ips.isEmpty()) {
                    send(sender, "banip.no-ip-found", Map.of("target", resolved.getName()));
                    return;
                }
                String ip = ips.get(ips.size() - 1);
                guardedApply(sender, resolved.getUuid(), resolved.getName(), ip, staffUuid, staffName, reason);
            }));
    }

    private void guardedApply(CommandSender sender, UUID targetUuid, String displayName, String ip, UUID staffUuid, String staffName, String reason) {
        plugin.getStaffExemptionService().hasPunishmentExemption(targetUuid, "omnibans.exempt.banip").thenAccept(exempt -> {
            if (exempt) {
                send(sender, "exempt.cannot-punish", Map.of("target", displayName));
                return;
            }
            applyBan(sender, ip, displayName, staffUuid, staffName, reason);
        });
    }

    private void applyBan(CommandSender sender, String ip, String displayName, UUID staffUuid, String staffName, String reason) {
        plugin.getPunishmentService().ban(null, displayName, ip, staffUuid, staffName, reason, -1L, true).thenAccept(punishment -> {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", displayName);
            placeholders.put("reason", reason);
            send(sender, "banip.success", placeholders);
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(onlinePlayerNames(), args[0]);
        }
        if (args.length == 2) {
            return filterPrefix(reasonSuggestions(), args[1]);
        }
        return Collections.emptyList();
    }

}
