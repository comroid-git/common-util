package org.comroid.uniform.adapter.data.json.jackson;

import org.comroid.common.annotation.Instance;
import org.comroid.uniform.data.SerializationAdapter;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JacksonLib extends SerializationAdapter<JsonNode, ObjectNode, ArrayNode> {
    public static @Instance final JacksonLib jacksonLib = new JacksonLib();
    public static final ObjectMapper objectMapper = new ObjectMapper();

    protected JacksonLib() {
        super("application/json", ObjectNode.class, ArrayNode.class);
    }

    @Override
    public UniNode parse(String data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UniObjectNode createUniObjectNode(ObjectNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UniArrayNode createUniArrayNode(ArrayNode node) {
        throw new UnsupportedOperationException();
    }
}