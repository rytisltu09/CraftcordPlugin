package org.craftcord.craftcordplugin.websocket;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.websocket.WsContext;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.minecraft.MinecraftService;
import org.craftcord.craftcordplugin.protocol.JsonSupport;
import org.craftcord.craftcordplugin.service.ActionRouter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebSocketMessageHandlerTest {

    @Test
    void supportsAuthValidateRequestAndMarksSessionAuthenticated() {
        WebSocketSessionRegistry sessions = new WebSocketSessionRegistry();
        WebSocketMessageHandler handler = new WebSocketMessageHandler(
                Logger.getAnonymousLogger(),
                new JsonSupport(),
                new AuthService("secret"),
                new ActionRouter(new AuthService("secret"), new NoopService()),
                sessions,
                false
        );

        WsContext context = mock(WsContext.class);
        handler.onMessage(context, "{\"type\":\"request\",\"id\":\"1\",\"action\":\"auth.validate\",\"payload\":{\"token\":\"secret\"}}\n");

        verify(context, times(1)).send(contains("\"status\":\"ok\""));
        org.junit.jupiter.api.Assertions.assertTrue(sessions.isAuthenticated(context));
    }

    @Test
    void rejectsInvalidRequestType() {
        WebSocketSessionRegistry sessions = new WebSocketSessionRegistry();
        WebSocketMessageHandler handler = new WebSocketMessageHandler(
                Logger.getAnonymousLogger(),
                new JsonSupport(),
                new AuthService("secret"),
                new ActionRouter(new AuthService("secret"), new NoopService()),
                sessions,
                false
        );

        WsContext context = mock(WsContext.class);
        when(context.header("Authorization")).thenReturn("Bearer secret");
        handler.onConnect(context);
        handler.onMessage(context, "{\"type\":\"event\",\"id\":\"1\",\"action\":\"minecraft.get_players\",\"payload\":{}}\n");

        verify(context, times(1)).send(contains("bad_request"));
    }

    private static final class NoopService implements MinecraftService {
        @Override
        public CompletableFuture<ObjectNode> sendMessage(com.fasterxml.jackson.databind.JsonNode payload) {
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode());
        }

        @Override
        public CompletableFuture<ObjectNode> execute(com.fasterxml.jackson.databind.JsonNode payload) {
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode());
        }

        @Override
        public CompletableFuture<ObjectNode> getPlayers() {
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode());
        }

        @Override
        public CompletableFuture<ObjectNode> getServerInfo() {
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode());
        }

        @Override
        public CompletableFuture<ObjectNode> kick(com.fasterxml.jackson.databind.JsonNode payload) {
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode());
        }

        @Override
        public CompletableFuture<ObjectNode> ban(com.fasterxml.jackson.databind.JsonNode payload) {
            return CompletableFuture.completedFuture(JsonNodeFactory.instance.objectNode());
        }
    }
}

