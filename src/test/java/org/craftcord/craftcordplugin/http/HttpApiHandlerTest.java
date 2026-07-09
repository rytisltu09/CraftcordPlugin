package org.craftcord.craftcordplugin.http;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.minecraft.MinecraftService;
import org.craftcord.craftcordplugin.protocol.JsonSupport;
import org.craftcord.craftcordplugin.service.ActionRouter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpApiHandlerTest {

    @Test
    void returns200ForHealth() {
        HttpApiHandler handler = new HttpApiHandler(
                Logger.getAnonymousLogger(),
                new JsonSupport(),
                new AuthService("secret"),
                new ActionRouter(new AuthService("secret"), new NoopService()),
                false
        );

        Context context = mock(Context.class);
        when(context.status(anyInt())).thenReturn(context);

        handler.handleHealth(context);

        verify(context, times(1)).status(200);
        verify(context, times(1)).result(org.mockito.ArgumentMatchers.contains("\"status\":\"ok\""));
    }

    @Test
    void returns401WhenAuthHeaderMissing() {
        HttpApiHandler handler = new HttpApiHandler(
                Logger.getAnonymousLogger(),
                new JsonSupport(),
                new AuthService("secret"),
                new ActionRouter(new AuthService("secret"), new NoopService()),
                false
        );

        Context context = mock(Context.class);
        when(context.status(anyInt())).thenReturn(context);

        handler.handleAuthValidate(context);

        verify(context, times(1)).status(401);
        verify(context, times(1)).result(org.mockito.ArgumentMatchers.contains("auth_failed"));
    }

    @Test
    void returnsUnsupportedActionErrorForUnknownRpcAction() {
        HttpApiHandler handler = new HttpApiHandler(
                Logger.getAnonymousLogger(),
                new JsonSupport(),
                new AuthService("secret"),
                new ActionRouter(new AuthService("secret"), new NoopService()),
                false
        );

        Context context = mock(Context.class);
        when(context.header("Authorization")).thenReturn("Bearer secret");
        when(context.body()).thenReturn("{\"action\":\"minecraft.unknown\",\"payload\":{}}\n");
        when(context.status(anyInt())).thenReturn(context);

        handler.handleRpc(context);

        verify(context, times(1)).status(400);
        verify(context, times(1)).result(org.mockito.ArgumentMatchers.contains("unsupported_action"));
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

