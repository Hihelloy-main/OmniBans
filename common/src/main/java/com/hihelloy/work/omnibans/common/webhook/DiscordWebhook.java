package com.hihelloy.work.omnibans.common.webhook;

import com.hihelloy.work.omnibans.common.util.PluginLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class DiscordWebhook {

    private final String webhookUrl;
    private final PluginLogger logger;
    private final HttpClient client;

    public DiscordWebhook(String webhookUrl, PluginLogger logger) {
        this.webhookUrl = webhookUrl;
        this.logger = logger;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public void send(String title, String description, int color) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        String escapedTitle = escape(title);
        String escapedDescription = escape(description);
        String payload = "{\"embeds\":[{\"title\":\"" + escapedTitle + "\",\"description\":\"" + escapedDescription + "\",\"color\":" + color + "}]}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .exceptionally(throwable -> {
                logger.warn("Failed to send discord webhook: " + throwable.getMessage());
                return null;
            });
    }

    private String escape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

}
