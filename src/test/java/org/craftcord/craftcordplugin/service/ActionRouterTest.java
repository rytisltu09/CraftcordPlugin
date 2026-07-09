package org.craftcord.craftcordplugin.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.minecraft.MinecraftService;
import org.craftcord.craftcordplugin.protocol.ApiException;
import org.craftcord.craftcordplugin.protocol.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionRouterTest {

    @Test
    void rejectsUnsupportedAction() {
        ActionRouter router = new ActionRouter(new AuthService("secret"), new NoopService());

        ApiException ex = assertThrows(ApiException.class,
                () -> router.route("minecraft.unknown", JsonNodeFactory.instance.objectNode(), true));

        assertEquals(ErrorCode.UNSUPPORTED_ACTION, ex.code());
    }

    @Test
    void routesGetPlayersAction() {
        ActionRouter router = new ActionRouter(new AuthService("secret"), new NoopService());
        ObjectNode result = router.route("minecraft.get_players", JsonNodeFactory.instance.objectNode(), true).join();
        assertEquals(0, result.get("players").size());
    }

    @Test
    void validatesTokenThroughAction() {
        ActionRouter router = new ActionRouter(new AuthService("secret"), new NoopService());

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("token", "secret");

        ObjectNode result = router.route("auth.validate", payload, false).join();
        assertEquals(true, result.get("authenticated").asBoolean());
    }

    @Test
    void rejectsUnauthenticatedMinecraftActions() {
        ActionRouter router = new ActionRouter(new AuthService("secret"), new NoopService());

        ApiException ex = assertThrows(ApiException.class,
                () -> router.route("minecraft.get_players", JsonNodeFactory.instance.objectNode(), false));

        assertEquals(ErrorCode.AUTH_FAILED, ex.code());
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
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.set("players", JsonNodeFactory.instance.arrayNode());
            return CompletableFuture.completedFuture(node);
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

