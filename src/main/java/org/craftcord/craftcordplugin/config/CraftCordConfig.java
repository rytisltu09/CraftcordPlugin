package org.craftcord.craftcordplugin.config;

public record CraftCordConfig(
        String host,
        int port,
        String websocketPath,
        String httpBasePath,
        String apiToken,
        boolean enableHttp,
        boolean enableWebSocket,
        boolean logRequests,
        boolean logEvents
) {
    public CraftCordConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        if (websocketPath == null || websocketPath.isBlank() || websocketPath.charAt(0) != '/') {
            throw new IllegalArgumentException("websocketPath must start with '/'");
        }
        if (httpBasePath == null || httpBasePath.isBlank() || httpBasePath.charAt(0) != '/') {
            throw new IllegalArgumentException("httpBasePath must start with '/'");
        }
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalArgumentException("apiToken must not be blank");
        }
    }
}

