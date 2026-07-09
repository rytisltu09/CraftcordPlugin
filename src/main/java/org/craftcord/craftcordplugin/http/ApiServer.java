package org.craftcord.craftcordplugin.http;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.craftcord.craftcordplugin.config.CraftCordConfig;
import org.craftcord.craftcordplugin.protocol.JsonSupport;
import org.craftcord.craftcordplugin.websocket.WebSocketMessageHandler;

public final class ApiServer {
    private final CraftCordConfig config;
    private final JsonSupport jsonSupport;
    private final HttpApiHandler httpApiHandler;
    private final WebSocketMessageHandler webSocketMessageHandler;

    private Javalin app;

    public ApiServer(
            CraftCordConfig config,
            JsonSupport jsonSupport,
            HttpApiHandler httpApiHandler,
            WebSocketMessageHandler webSocketMessageHandler
    ) {
        this.config = config;
        this.jsonSupport = jsonSupport;
        this.httpApiHandler = httpApiHandler;
        this.webSocketMessageHandler = webSocketMessageHandler;
    }

    public void start() {
        this.app = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
            cfg.jsonMapper(new JavalinJackson(jsonSupport.mapper(), false));
            cfg.showJavalinBanner = false;
        });

        if (config.enableHttp()) {
            String base = normalizedBasePath();
            app.get(base + "/health", httpApiHandler::handleHealth);
            app.get(base + "/auth/validate", httpApiHandler::handleAuthValidate);
            app.post(base + "/rpc", httpApiHandler::handleRpc);
        }

        if (config.enableWebSocket()) {
            app.ws(config.websocketPath(), ws -> {
                ws.onConnect(webSocketMessageHandler::onConnect);
                ws.onMessage(ctx -> webSocketMessageHandler.onMessage(ctx, ctx.message()));
                ws.onClose(ctx -> webSocketMessageHandler.onClose(ctx));
                ws.onError(ctx -> webSocketMessageHandler.onError(ctx, ctx.error()));
            });
        }

        app.start(config.host(), config.port());
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private String normalizedBasePath() {
        return config.httpBasePath().endsWith("/")
                ? config.httpBasePath().substring(0, config.httpBasePath().length() - 1)
                : config.httpBasePath();
    }
}

