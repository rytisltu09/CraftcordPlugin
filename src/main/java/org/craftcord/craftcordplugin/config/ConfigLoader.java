package org.craftcord.craftcordplugin.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

public final class ConfigLoader {
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final String GLOBAL_HOST = "0.0.0.0";

    public CraftCordConfig load(FileConfiguration config) {
        String bindMode = config.getString("bindMode", "local");
        String configuredHost = config.getString("host", "");

        return new CraftCordConfig(
                resolveHost(bindMode, configuredHost),
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

    private String resolveHost(String bindMode, String configuredHost) {
        if (configuredHost != null && !configuredHost.isBlank()) {
            return configuredHost;
        }

        if (bindMode == null || bindMode.isBlank()) {
            return LOCAL_HOST;
        }

        return switch (bindMode.trim().toLowerCase(Locale.ROOT)) {
            case "local" -> LOCAL_HOST;
            case "global" -> GLOBAL_HOST;
            default -> throw new IllegalArgumentException("bindMode must be either 'local' or 'global'");
        };
    }
}

