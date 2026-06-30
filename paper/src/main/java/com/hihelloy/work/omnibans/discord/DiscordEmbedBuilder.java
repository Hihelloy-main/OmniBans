package com.hihelloy.work.omnibans.discord;

import java.util.ArrayList;
import java.util.List;

public final class DiscordEmbedBuilder {

    private String title = "";
    private int color = 0xFF0000;
    private String roleIdToPing;
    private final List<String[]> fields = new ArrayList<>();

    public DiscordEmbedBuilder title(String title) {
        this.title = title;
        return this;
    }

    public DiscordEmbedBuilder color(int color) {
        this.color = color;
        return this;
    }

    public DiscordEmbedBuilder pingRole(String roleId) {
        this.roleIdToPing = roleId;
        return this;
    }

    public DiscordEmbedBuilder field(String name, String value) {
        fields.add(new String[] {name, value});
        return this;
    }

    public String build() {
        StringBuilder fieldsJson = new StringBuilder();
        for (int index = 0; index < fields.size(); index++) {
            String[] field = fields.get(index);
            if (index > 0) {
                fieldsJson.append(",");
            }
            fieldsJson.append("{\"name\":\"").append(escape(field[0])).append("\",\"value\":\"").append(escape(field[1])).append("\",\"inline\":false}");
        }
        String content = roleIdToPing != null && !roleIdToPing.isBlank() ? "<@&" + roleIdToPing + ">" : "";
        return "{\"content\":\"" + escape(content) + "\",\"embeds\":[{\"title\":\"" + escape(title) + "\",\"color\":" + color + ",\"fields\":[" + fieldsJson + "]}]}";
    }

    private String escape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

}
