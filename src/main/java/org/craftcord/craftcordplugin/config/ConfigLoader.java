package org.craftcord.craftcordplugin.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigLoader {
    public CraftCordConfig load(FileConfiguration config) {
        return new CraftCordConfig(
                config.getString("host", "0.0.0.0"),
                config.getInt("port", 8080),
                config.getString("websocketPath", "/ws"),
                config.getString("httpBasePath", "/api/v1"),
                config.getString("apiToken", "change-me"),
                config.getBoolean("enableHttp", true),
                config.getBoolean("enableWebSocket", true),
                config.getBoolean("logRequests", false),
                config.getBoolean("logEvents", false)
        );
    }
}

