package com.hihelloy.work.omnibans.discord;

import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class DiscordBotClient {

    private final String botToken;
    private final String channelId;
    private final PluginLogger logger;
    private final HttpClient httpClient;

    public DiscordBotClient(String botToken, String channelId, PluginLogger logger) {
        this.botToken = botToken;
        this.channelId = channelId;
        this.logger = logger;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public void sendEmbed(String payload) {
        if (botToken == null || botToken.isBlank() || channelId == null || channelId.isBlank()) {
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://discord.com/api/v10/channels/" + channelId + "/messages"))
            .header("Authorization", "Bot " + botToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(this::logIfFailed)
            .exceptionally(throwable -> {
                logger.warn("Failed to send discord bot message: " + throwable.getMessage());
                return null;
            });
    }

    private void logIfFailed(HttpResponse<String> response) {
        if (response.statusCode() >= 300) {
            logger.warn("Discord bot returned status " + response.statusCode() + ": " + response.body());
        }
    }

}
