
package org.craftcord.craftcordplugin.model;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class PlayerPayloadMapper {
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    public ObjectNode toNode(PlayerModel player) {
        ObjectNode playerNode = JSON.objectNode();
        playerNode.put("uuid", player.uuid());
        playerNode.put("username", player.username());
        playerNode.put("health", player.health());
        playerNode.put("world", player.world());

        ObjectNode locationNode = JSON.objectNode();
        locationNode.put("x", player.location().x());
        locationNode.put("y", player.location().y());
        locationNode.put("z", player.location().z());
        locationNode.put("yaw", player.location().yaw());
        locationNode.put("pitch", player.location().pitch());

        playerNode.set("location", locationNode);
        return playerNode;
    }
}

