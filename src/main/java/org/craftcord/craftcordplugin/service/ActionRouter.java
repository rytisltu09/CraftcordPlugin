package org.craftcord.craftcordplugin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.minecraft.MinecraftService;
import org.craftcord.craftcordplugin.protocol.ApiException;
import org.craftcord.craftcordplugin.protocol.ErrorCode;

import java.util.concurrent.CompletableFuture;

public final class ActionRouter {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final AuthService authService;
    private final MinecraftService minecraftService;

    public ActionRouter(AuthService authService, MinecraftService minecraftService) {
        this.authService = authService;
        this.minecraftService = minecraftService;
    }

    public CompletableFuture<ObjectNode> route(String action, JsonNode payload, boolean authenticated) {
        if (action == null || action.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Field 'action' is required");
        }

        if ("auth.validate".equals(action)) {
            return validate(payload);
        }

        if (!authenticated) {
            throw new ApiException(ErrorCode.AUTH_FAILED, "Invalid token");
        }

        return switch (action) {
            case "minecraft.send_message" -> minecraftService.sendMessage(payload);
            case "minecraft.execute" -> minecraftService.execute(payload);
            case "minecraft.get_players" -> minecraftService.getPlayers();
            case "minecraft.get_server_info" -> minecraftService.getServerInfo();
            case "minecraft.kick" -> minecraftService.kick(payload);
            case "minecraft.ban" -> minecraftService.ban(payload);
            default -> throw new ApiException(ErrorCode.UNSUPPORTED_ACTION, "Unknown action");
        };
    }

    private CompletableFuture<ObjectNode> validate(JsonNode payload) {
        if (payload == null || payload.isNull() || payload.get("token") == null) {
            throw new ApiException(ErrorCode.AUTH_FAILED, "Invalid token");
        }
        String token = payload.get("token").asText("");
        if (!authService.isValidToken(token)) {
            throw new ApiException(ErrorCode.AUTH_FAILED, "Invalid token");
        }

        ObjectNode data = JSON.objectNode();
        data.put("authenticated", true);
        return CompletableFuture.completedFuture(data);
    }
}

