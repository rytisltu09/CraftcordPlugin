package org.craftcord.craftcordplugin.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.websocket.WsContext;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.protocol.ApiException;
import org.craftcord.craftcordplugin.protocol.EnvelopeFactory;
import org.craftcord.craftcordplugin.protocol.ErrorCode;
import org.craftcord.craftcordplugin.protocol.JsonSupport;
import org.craftcord.craftcordplugin.service.ActionRouter;

import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

public final class WebSocketMessageHandler {
    private final Logger logger;
    private final JsonSupport jsonSupport;
    private final AuthService authService;
    private final ActionRouter actionRouter;
    private final WebSocketSessionRegistry sessions;
    private final boolean logRequests;

    public WebSocketMessageHandler(
            Logger logger,
            JsonSupport jsonSupport,
            AuthService authService,
            ActionRouter actionRouter,
            WebSocketSessionRegistry sessions,
            boolean logRequests
    ) {
        this.logger = logger;
        this.jsonSupport = jsonSupport;
        this.authService = authService;
        this.actionRouter = actionRouter;
        this.sessions = sessions;
        this.logRequests = logRequests;
    }

    public void onConnect(WsContext context) {
        String authorization = context.header("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            if (authService.isValidAuthorizationHeader(authorization)) {
                sessions.markAuthenticated(context);
            } else {
                context.send(jsonSupport.writeValue(EnvelopeFactory.wsError("", ErrorCode.AUTH_FAILED, "Invalid token")));
                context.closeSession(1008, "Invalid token");
            }
        }
    }

    public void onMessage(WsContext context, String rawMessage) {
        try {
            JsonNode root = jsonSupport.readTree(rawMessage);
            String id = root.path("id").asText("");
            String type = root.path("type").asText("");
            String action = root.path("action").asText("");
            JsonNode payload = root.path("payload");

            if (!"request".equals(type)) {
                send(context, EnvelopeFactory.wsError(id, ErrorCode.BAD_REQUEST, "Invalid message type"));
                return;
            }

            if (logRequests) {
                logger.info(() -> "CraftCord WS action=" + action + " id=" + id);
            }

            boolean authenticated = sessions.isAuthenticated(context);
            actionRouter.route(action, payload, authenticated)
                    .thenAccept(data -> {
                        if ("auth.validate".equals(action)) {
                            sessions.markAuthenticated(context);
                        }
                        send(context, EnvelopeFactory.wsSuccess(id, data));
                    })
                    .exceptionally(throwable -> {
                        handleThrowable(context, id, throwable);
                        return null;
                    });
        } catch (ApiException ex) {
            send(context, EnvelopeFactory.wsError("", ex.code(), ex.getMessage()));
        } catch (Exception ex) {
            send(context, EnvelopeFactory.wsError("", ErrorCode.INTERNAL_ERROR, "Internal error"));
            logger.warning("Unexpected WS processing error: " + ex.getMessage());
        }
    }

    public void onClose(WsContext context) {
        sessions.remove(context);
    }

    public void onError(WsContext context, Throwable throwable) {
        sessions.remove(context);
        logger.warning("CraftCord WS session error: " + throwable.getMessage());
    }

    private void handleThrowable(WsContext context, String id, Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
        if (cause instanceof ApiException apiException) {
            send(context, EnvelopeFactory.wsError(id, apiException.code(), apiException.getMessage()));
            return;
        }
        send(context, EnvelopeFactory.wsError(id, ErrorCode.INTERNAL_ERROR, "Internal error"));
        logger.warning("CraftCord WS action failure: " + cause.getMessage());
    }

    private void send(WsContext context, ObjectNode node) {
        context.send(jsonSupport.writeValue(node));
    }
}

