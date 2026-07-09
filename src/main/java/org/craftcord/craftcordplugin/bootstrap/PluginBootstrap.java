package org.craftcord.craftcordplugin.bootstrap;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.craftcord.craftcordplugin.auth.AuthService;
import org.craftcord.craftcordplugin.config.ConfigLoader;
import org.craftcord.craftcordplugin.config.CraftCordConfig;
import org.craftcord.craftcordplugin.events.PaperEventListener;
import org.craftcord.craftcordplugin.http.ApiServer;
import org.craftcord.craftcordplugin.http.HttpApiHandler;
import org.craftcord.craftcordplugin.minecraft.MainThreadExecutor;
import org.craftcord.craftcordplugin.minecraft.MinecraftService;
import org.craftcord.craftcordplugin.minecraft.PaperMinecraftService;
import org.craftcord.craftcordplugin.model.ModelMapper;
import org.craftcord.craftcordplugin.protocol.JsonSupport;
import org.craftcord.craftcordplugin.service.ActionRouter;
import org.craftcord.craftcordplugin.websocket.EventPublisher;
import org.craftcord.craftcordplugin.websocket.WebSocketMessageHandler;
import org.craftcord.craftcordplugin.websocket.WebSocketSessionRegistry;

import java.time.Instant;
import java.util.logging.Logger;

public final class PluginBootstrap {
    private final JavaPlugin plugin;
    private final Logger logger;

    private ApiServer apiServer;
    private PaperEventListener paperEventListener;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void start() {
        plugin.saveDefaultConfig();

        CraftCordConfig config = new ConfigLoader().load(plugin.getConfig());
        warnOnInsecureDefaultToken(config.apiToken());

        JsonSupport jsonSupport = new JsonSupport();
        AuthService authService = new AuthService(config.apiToken());
        MainThreadExecutor mainThreadExecutor = new MainThreadExecutor(plugin);
        ModelMapper modelMapper = new ModelMapper();

        MinecraftService minecraftService = new PaperMinecraftService(mainThreadExecutor, modelMapper, Instant.now());
        ActionRouter actionRouter = new ActionRouter(authService, minecraftService);
        WebSocketSessionRegistry sessions = new WebSocketSessionRegistry();
        EventPublisher eventPublisher = new EventPublisher(logger, jsonSupport, sessions, config.logEvents());

        WebSocketMessageHandler webSocketMessageHandler = new WebSocketMessageHandler(
                logger,
                jsonSupport,
                authService,
                actionRouter,
                sessions,
                config.logRequests()
        );

        HttpApiHandler httpApiHandler = new HttpApiHandler(
                logger,
                jsonSupport,
                authService,
                actionRouter,
                config.logRequests()
        );

        this.apiServer = new ApiServer(config, jsonSupport, httpApiHandler, webSocketMessageHandler);
        this.apiServer.start();

        this.paperEventListener = new PaperEventListener(mainThreadExecutor, modelMapper, eventPublisher);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(paperEventListener, plugin);

        paperEventListener.publishServerStart();
        logger.info("CraftCord API started on " + config.host() + ":" + config.port());
    }

    public void stop() {
        if (paperEventListener != null) {
            paperEventListener.publishServerStop("shutdown");
        }

        if (apiServer != null) {
            apiServer.stop();
        }
    }

    private void warnOnInsecureDefaultToken(String token) {
        if ("change-me".equals(token)) {
            logger.warning("CraftCord apiToken is using default value. Update config.yml before production use.");
        }
    }
}

