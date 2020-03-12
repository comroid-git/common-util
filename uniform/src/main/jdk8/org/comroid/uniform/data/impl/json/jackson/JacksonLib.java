package org.comroid.uniform.data.impl.json.jackson;

import org.comroid.uniform.data.SeriLib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JacksonLib extends SeriLib<JsonNode, ObjectNode, ArrayNode> {
    public static final JacksonLib instance;

    static {
        try {
            Class.forName("com.fasterxml.jackson.databind.JsonNode");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot initialize JacksonLib: Missing dependency class", e);
        } finally {
            instance = new JacksonLib();
        }
    }

    private JacksonLib() {
        super(ObjectNode.class, ArrayNode.class);
    }
}
