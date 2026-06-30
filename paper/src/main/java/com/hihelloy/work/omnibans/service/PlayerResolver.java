package com.hihelloy.work.omnibans.service;

import com.hihelloy.work.omnibans.OmniBans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class PlayerResolver {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}$");

    private final OmniBans plugin;
    private final HttpClient httpClient;

    public PlayerResolver(OmniBans plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public CompletableFuture<ResolvedPlayer> resolve(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return CompletableFuture.completedFuture(new ResolvedPlayer(online.getUniqueId(), online.getName(), true));
        }
        if (UUID_PATTERN.matcher(input).matches()) {
            UUID uuid = normalizeUuid(input);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : input;
            return CompletableFuture.completedFuture(new ResolvedPlayer(uuid, name, false));
        }
        return CompletableFuture.supplyAsync(() -> resolveOffline(input), plugin.getAsyncExecutor());
    }

    private ResolvedPlayer resolveOffline(String name) {
        Optional<ResolvedPlayer> mojang = lookupMojang(name);
        if (mojang.isPresent()) {
            return mojang.get();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        return new ResolvedPlayer(offlinePlayer.getUniqueId(), name, false);
    }

    private Optional<ResolvedPlayer> lookupMojang(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            String body = response.body();
            String id = extract(body, "\"id\":\"", "\"");
            String correctedName = extract(body, "\"name\":\"", "\"");
            if (id == null) {
                return Optional.empty();
            }
            UUID uuid = normalizeUuid(id);
            return Optional.of(new ResolvedPlayer(uuid, correctedName != null ? correctedName : name, false));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String extract(String body, String prefix, String suffix) {
        int start = body.indexOf(prefix);
        if (start == -1) {
            return null;
        }
        int from = start + prefix.length();
        int end = body.indexOf(suffix, from);
        if (end == -1) {
            return null;
        }
        return body.substring(from, end);
    }

    private UUID normalizeUuid(String input) {
        String cleaned = input.replace("-", "");
        StringBuilder builder = new StringBuilder(cleaned);
        builder.insert(20, "-");
        builder.insert(16, "-");
        builder.insert(12, "-");
        builder.insert(8, "-");
        return UUID.fromString(builder.toString());
    }

    public static final class ResolvedPlayer {

        private final UUID uuid;
        private final String name;
        private final boolean online;

        public ResolvedPlayer(UUID uuid, String name, boolean online) {
            this.uuid = uuid;
            this.name = name;
            this.online = online;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public boolean isOnline() {
            return online;
        }

    }

}
