package org.craftcord.craftcordplugin.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.CompletableFuture;

public interface MinecraftService {
    CompletableFuture<ObjectNode> sendMessage(JsonNode payload);

    CompletableFuture<ObjectNode> execute(JsonNode payload);

    CompletableFuture<ObjectNode> getPlayers();

    CompletableFuture<ObjectNode> getServerInfo();

    CompletableFuture<ObjectNode> kick(JsonNode payload);

    CompletableFuture<ObjectNode> ban(JsonNode payload);
}

