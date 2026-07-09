package org.craftcord.craftcordplugin.protocol;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class EnvelopeFactory {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private EnvelopeFactory() {
    }

    public static ObjectNode wsSuccess(String id, ObjectNode data) {
        ObjectNode root = JSON.objectNode();
        root.put("type", "response");
        root.put("id", id == null ? "" : id);
        root.put("status", "ok");
        root.set("data", data == null ? JSON.objectNode() : data);
        return root;
    }

    public static ObjectNode wsError(String id, ErrorCode code, String error) {
        ObjectNode root = JSON.objectNode();
        root.put("type", "response");
        root.put("id", id == null ? "" : id);
        root.put("status", "error");
        root.put("code", code.code());
        root.put("error", error);
        return root;
    }

    public static ObjectNode event(String eventName, ObjectNode data) {
        ObjectNode root = JSON.objectNode();
        root.put("type", "event");
        root.put("event", eventName);
        root.set("data", data == null ? JSON.objectNode() : data);
        return root;
    }

    public static ObjectNode httpSuccess(ObjectNode data) {
        ObjectNode root = JSON.objectNode();
        root.put("status", "ok");
        root.set("data", data == null ? JSON.objectNode() : data);
        return root;
    }

    public static ObjectNode httpError(ErrorCode code, String message) {
        ObjectNode root = JSON.objectNode();
        root.put("status", "error");
        root.put("code", code.code());
        root.put("error", message);
        return root;
    }
}

