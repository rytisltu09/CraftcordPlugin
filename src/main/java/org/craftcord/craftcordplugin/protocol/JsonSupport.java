package org.craftcord.craftcordplugin.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonSupport {
    private final ObjectMapper mapper;

    public JsonSupport() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public JsonNode readTree(String raw) {
        try {
            return mapper.readTree(raw);
        } catch (JsonProcessingException ex) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Invalid JSON payload");
        }
    }

    public String writeValue(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to serialize response");
        }
    }
}

