package org.craftcord.craftcordplugin.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.craftcord.craftcordplugin.model.ModelMapper;
import org.craftcord.craftcordplugin.model.PlayerPayloadMapper;
import org.craftcord.craftcordplugin.model.ServerInfoModel;
import org.craftcord.craftcordplugin.protocol.ApiException;
import org.craftcord.craftcordplugin.protocol.ErrorCode;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class PaperMinecraftService implements MinecraftService {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final MainThreadExecutor mainThreadExecutor;
    private final ModelMapper modelMapper;
    private final Instant startedAt;
    private final PlayerPayloadMapper payloadMapper;

    public PaperMinecraftService(MainThreadExecutor mainThreadExecutor, ModelMapper modelMapper, Instant startedAt) {
        this.mainThreadExecutor = mainThreadExecutor;
        this.modelMapper = modelMapper;
        this.startedAt = startedAt;
        this.payloadMapper = new PlayerPayloadMapper();
    }

    @Override
    public CompletableFuture<ObjectNode> sendMessage(JsonNode payload) {
        String message = optionalText(payload, "message")
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "Field 'message' is required"));

        return mainThreadExecutor.supply(() -> {
            String target = optionalText(payload, "target").orElse(null);
            if (target == null || target.isBlank() || target.equalsIgnoreCase("all") || target.equalsIgnoreCase("server")) {
                Bukkit.broadcastMessage(message);
            } else {
                Player player = Bukkit.getPlayerExact(target);
                if (player == null) {
                    throw new ApiException(ErrorCode.PLAYER_NOT_FOUND, "Player is not online: " + target);
                }
                player.sendMessage(message);
            }
            return JSON.objectNode();
        });
    }

    @Override
    public CompletableFuture<ObjectNode> execute(JsonNode payload) {
        String command = optionalText(payload, "command")
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "Field 'command' is required"));

        return mainThreadExecutor.supply(() -> {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            boolean success = Bukkit.dispatchCommand(console, command);
            ObjectNode data = JSON.objectNode();
            data.put("success", success);
            return data;
        });
    }

    @Override
    public CompletableFuture<ObjectNode> getPlayers() {
        return mainThreadExecutor.supply(() -> {
            ArrayNode playersArray = JSON.arrayNode();
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            for (Player player : players) {
                playersArray.add(payloadMapper.toNode(modelMapper.toPlayerModel(player)));
            }
            ObjectNode data = JSON.objectNode();
            data.set("players", playersArray);
            return data;
        });
    }

    @Override
    public CompletableFuture<ObjectNode> getServerInfo() {
        return mainThreadExecutor.supply(() -> {
            ServerInfoModel info = new ServerInfoModel(
                    Bukkit.getVersion(),
                    Bukkit.getOnlinePlayers().size(),
                    Bukkit.getMaxPlayers(),
                    (System.currentTimeMillis() - startedAt.toEpochMilli()) / 1000.0
            );

            ObjectNode data = JSON.objectNode();
            data.put("version", info.version());
            data.put("online_players", info.onlinePlayers());
            data.put("max_players", info.maxPlayers());
            data.put("uptime", info.uptime());
            return data;
        });
    }

    @Override
    public CompletableFuture<ObjectNode> kick(JsonNode payload) {
        String username = optionalText(payload, "username")
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "Field 'username' is required"));
        String reason = optionalText(payload, "reason").orElse("Kicked by CraftCord");

        return mainThreadExecutor.supply(() -> {
            Player player = Bukkit.getPlayerExact(username);
            if (player == null) {
                throw new ApiException(ErrorCode.PLAYER_NOT_FOUND, "Player is not online: " + username);
            }
            player.kickPlayer(reason);
            ObjectNode data = JSON.objectNode();
            data.put("success", true);
            return data;
        });
    }

    @Override
    public CompletableFuture<ObjectNode> ban(JsonNode payload) {
        String username = optionalText(payload, "username")
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "Field 'username' is required"));
        String reason = optionalText(payload, "reason").orElse("Banned by CraftCord");

        return mainThreadExecutor.supply(() -> {
            Bukkit.getBanList(BanList.Type.NAME).addBan(username, reason, (Date) null, "CraftCordPlugin");
            Player online = Bukkit.getPlayerExact(username);
            if (online != null) {
                online.kickPlayer(reason);
            }
            ObjectNode data = JSON.objectNode();
            data.put("success", true);
            return data;
        });
    }

    private static Optional<String> optionalText(JsonNode payload, String field) {
        if (payload == null || payload.isNull()) {
            return Optional.empty();
        }
        JsonNode value = payload.get(field);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asText());
    }

}

