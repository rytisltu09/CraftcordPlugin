package org.craftcord.craftcordplugin.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventPayloadMappingTest {

    @Test
    void mapsPlayerObjectToProtocolShape() {
        PlayerModel player = new PlayerModel(
                "550e8400-e29b-41d4-a716-446655440000",
                "Alex",
                20.0,
                "world",
                new LocationModel(1.0, 64.0, 2.0, 0.0f, 0.0f)
        );

        PlayerPayloadMapper mapper = new PlayerPayloadMapper();
        ObjectNode node = mapper.toNode(player);

        assertEquals("Alex", node.get("username").asText());
        assertEquals("world", node.get("world").asText());
        assertEquals(64.0, node.get("location").get("y").asDouble());
    }
}

