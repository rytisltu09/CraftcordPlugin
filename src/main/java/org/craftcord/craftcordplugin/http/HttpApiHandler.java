package org.craftcord.craftcordplugin.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.protocol.ApiException;
import org.craftcord.craftcordplugin.protocol.EnvelopeFactory;
import org.craftcord.craftcordplugin.protocol.ErrorCode;
import org.craftcord.craftcordplugin.protocol.JsonSupport;
import org.craftcord.craftcordplugin.service.ActionRouter;

import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

public final class HttpApiHandler {
    private final Logger logger;
    private final JsonSupport jsonSupport;
    private final AuthService authService;
    private final ActionRouter actionRouter;
    private final boolean logRequests;

    public HttpApiHandler(
            Logger logger,
            JsonSupport jsonSupport,
            AuthService authService,
            ActionRouter actionRouter,
            boolean logRequests
    ) {
        this.logger = logger;
        this.jsonSupport = jsonSupport;
        this.authService = authService;
        this.actionRouter = actionRouter;
        this.logRequests = logRequests;
    }

    public void handleAuthValidate(Context context) {
        String authorization = context.header("Authorization");
        if (!authService.isValidAuthorizationHeader(authorization)) {
            context.status(401).result(jsonSupport.writeValue(EnvelopeFactory.httpError(ErrorCode.AUTH_FAILED, "Invalid token")));
            return;
        }

        ObjectNode data = jsonSupport.mapper().createObjectNode();
        data.put("authenticated", true);
        context.status(200).result(jsonSupport.writeValue(EnvelopeFactory.httpSuccess(data)));
    }

    public void handleHealth(Context context) {
        ObjectNode data = jsonSupport.mapper().createObjectNode();
        data.put("status", "ok");
        context.status(200).result(jsonSupport.writeValue(EnvelopeFactory.httpSuccess(data)));
    }

    public void handleRpc(Context context) {
        String authorization = context.header("Authorization");
        if (!authService.isValidAuthorizationHeader(authorization)) {
            context.status(401).result(jsonSupport.writeValue(EnvelopeFactory.httpError(ErrorCode.AUTH_FAILED, "Invalid token")));
            return;
        }

        JsonNode body = jsonSupport.readTree(context.body());
        String action = body.path("action").asText("");
        JsonNode payload = body.path("payload");

        if (logRequests) {
            logger.info(() -> "CraftCord HTTP action=" + action);
        }

        try {
            ObjectNode data = actionRouter.route(action, payload, true).join();
            context.status(200).result(jsonSupport.writeValue(EnvelopeFactory.httpSuccess(data)));
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ApiException apiException) {
                int status = apiException.code() == ErrorCode.UNSUPPORTED_ACTION ? 400 : 500;
                if (apiException.code() == ErrorCode.BAD_REQUEST) {
                    status = 400;
                }
                context.status(status).result(jsonSupport.writeValue(
                        EnvelopeFactory.httpError(apiException.code(), apiException.getMessage())
                ));
                return;
            }
            context.status(500).result(jsonSupport.writeValue(EnvelopeFactory.httpError(ErrorCode.INTERNAL_ERROR, "Internal error")));
        } catch (ApiException ex) {
            int status = ex.code() == ErrorCode.BAD_REQUEST || ex.code() == ErrorCode.UNSUPPORTED_ACTION ? 400 : 500;
            context.status(status).result(jsonSupport.writeValue(EnvelopeFactory.httpError(ex.code(), ex.getMessage())));
        }
    }
}

