package org.craftcord.craftcordplugin.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocolSerializationTest {

    @Test
    void serializesAndDeserializesEnvelopes() {
        JsonSupport json = new JsonSupport();
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("authenticated", true);

        ObjectNode response = EnvelopeFactory.wsSuccess("abc-123", payload);
        String raw = json.writeValue(response);
        JsonNode parsed = json.readTree(raw);

        assertEquals("response", parsed.get("type").asText());
        assertEquals("abc-123", parsed.get("id").asText());
        assertEquals("ok", parsed.get("status").asText());
        assertEquals(true, parsed.get("data").get("authenticated").asBoolean());
    }
}

