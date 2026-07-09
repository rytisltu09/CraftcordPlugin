package org.craftcord.craftcordplugin.websocket;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.websocket.WsContext;
import org.craftcord.craftcordplugin.protocol.EnvelopeFactory;
import org.craftcord.craftcordplugin.protocol.JsonSupport;

import java.util.Set;
import java.util.logging.Logger;

public final class EventPublisher {
    private final Logger logger;
    private final JsonSupport jsonSupport;
    private final WebSocketSessionRegistry sessions;
    private final boolean logEvents;

    public EventPublisher(Logger logger, JsonSupport jsonSupport, WebSocketSessionRegistry sessions, boolean logEvents) {
        this.logger = logger;
        this.jsonSupport = jsonSupport;
        this.sessions = sessions;
        this.logEvents = logEvents;
    }

    public void publish(String eventName, ObjectNode data) {
        String payload = jsonSupport.writeValue(EnvelopeFactory.event(eventName, data));
        Set<WsContext> clients = sessions.authenticatedSessions();
        for (WsContext context : clients) {
            try {
                context.send(payload);
            } catch (Exception ex) {
                sessions.remove(context);
            }
        }

        if (logEvents) {
            logger.info(() -> "CraftCord event=" + eventName + " recipients=" + clients.size());
        }
    }
}

