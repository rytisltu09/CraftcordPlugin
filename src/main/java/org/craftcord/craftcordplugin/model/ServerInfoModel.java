package org.craftcord.craftcordplugin.model;

public record ServerInfoModel(
        String version,
        int onlinePlayers,
        int maxPlayers,
        double uptime
) {
}

