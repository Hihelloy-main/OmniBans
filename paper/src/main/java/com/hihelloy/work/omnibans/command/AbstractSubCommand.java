package com.hihelloy.work.omnibans.command;

import com.hihelloy.work.omnibans.OmniBans;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractSubCommand implements SubCommand {

    protected static final List<String> DURATION_SUGGESTIONS = List.of("30m", "1h", "6h", "12h", "1d", "3d", "7d", "14d", "30d", "permanent");
    protected static final List<String> PAGE_SUGGESTIONS = List.of("1", "2", "3");

    protected final OmniBans plugin;

    protected AbstractSubCommand(OmniBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (permission() != null && !sender.hasPermission(permission())) {
            send(sender, "no-permission", Map.of());
            return;
        }
        run(sender, args);
    }

    protected abstract void run(CommandSender sender, String[] args);

    protected void send(CommandSender sender, String path, Map<String, String> placeholders) {
        Component component = plugin.getMessages().component(path, placeholders);
        plugin.getMessageDispatcher().send(sender, component);
    }

    protected void usage(CommandSender sender) {
        send(sender, "invalid-usage", Map.of("usage", usage()));
    }

    protected List<String> filterPrefix(Collection<String> candidates, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(lower)) {
                result.add(candidate);
            }
        }
        return result;
    }

    protected List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    protected List<String> knownPlayerNames() {
        Set<String> names = new LinkedHashSet<>(onlinePlayerNames());
        names.addAll(plugin.getCache().getKnownNames());
        return new ArrayList<>(names);
    }

    protected List<String> bannedPlayerNames() {
        return new ArrayList<>(plugin.getCache().getBannedNames());
    }

    protected List<String> mutedPlayerNames() {
        return new ArrayList<>(plugin.getCache().getMutedNames());
    }

    protected List<String> reasonSuggestions() {
        return plugin.getOmniBansConfig().getCommonReasons();
    }

}
