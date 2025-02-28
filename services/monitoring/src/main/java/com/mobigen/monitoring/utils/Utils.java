package com.mobigen.monitoring.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    public JsonNode getJsonNode(String jsonStr) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        return mapper.readTree(jsonStr);
    }

    public String getAsTextOrNull(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull())
            return null;
        return jsonNode.asText();
    }

    public static boolean isContainer() {
        return System.getenv("DOCKER_CONTAINER") != null || System.getenv("KUBERNETES_SERVICE_HOST") != null;
    }
}
