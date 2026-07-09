package org.craftcord.craftcordplugin.websocket;

import io.javalin.websocket.WsContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WebSocketSessionRegistry {
    private final Set<WsContext> authenticatedSessions = ConcurrentHashMap.newKeySet();

    public void markAuthenticated(WsContext context) {
        authenticatedSessions.add(context);
    }

    public boolean isAuthenticated(WsContext context) {
        return authenticatedSessions.contains(context);
    }

    public void remove(WsContext context) {
        authenticatedSessions.remove(context);
    }

    public Set<WsContext> authenticatedSessions() {
        return Set.copyOf(authenticatedSessions);
    }
}

