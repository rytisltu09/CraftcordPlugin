package org.craftcord.craftcordplugin.events;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.craftcord.craftcordplugin.minecraft.MainThreadExecutor;
import org.craftcord.craftcordplugin.model.ModelMapper;
import org.craftcord.craftcordplugin.model.PlayerPayloadMapper;
import org.craftcord.craftcordplugin.websocket.EventPublisher;

public final class PaperEventListener implements Listener {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final MainThreadExecutor mainThreadExecutor;
    private final ModelMapper modelMapper;
    private final PlayerPayloadMapper payloadMapper;
    private final EventPublisher eventPublisher;

    public PaperEventListener(MainThreadExecutor mainThreadExecutor, ModelMapper modelMapper, EventPublisher eventPublisher) {
        this.mainThreadExecutor = mainThreadExecutor;
        this.modelMapper = modelMapper;
        this.payloadMapper = new PlayerPayloadMapper();
        this.eventPublisher = eventPublisher;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ObjectNode data = JSON.objectNode();
        data.set("player", payloadMapper.toNode(modelMapper.toPlayerModel(player)));
        eventPublisher.publish("player_join", data);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ObjectNode data = JSON.objectNode();
        data.set("player", payloadMapper.toNode(modelMapper.toPlayerModel(player)));
        eventPublisher.publish("player_leave", data);
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        Player player = event.getPlayer();

        mainThreadExecutor.run(() -> {
            ObjectNode data = JSON.objectNode();
            data.set("player", payloadMapper.toNode(modelMapper.toPlayerModel(player)));
            data.put("message", message);
            eventPublisher.publish("player_chat", data);
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        String reason = event.getDeathMessage() == null ? "" : event.getDeathMessage();

        mainThreadExecutor.run(() -> {
            ObjectNode data = JSON.objectNode();
            data.set("player", payloadMapper.toNode(modelMapper.toPlayerModel(player)));
            data.put("reason", reason);
            eventPublisher.publish("player_death", data);
        });
    }

    public void publishServerStart() {
        ObjectNode data = JSON.objectNode();
        data.put("version", Bukkit.getVersion());
        eventPublisher.publish("server_start", data);
    }

    public void publishServerStop(String reason) {
        ObjectNode data = JSON.objectNode();
        data.put("reason", reason);
        eventPublisher.publish("server_stop", data);
    }
}

