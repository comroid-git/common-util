package org.comroid.uniform.adapter.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.Nullable;

public abstract class JacksonAdapter extends SerializationAdapter<JsonNode, ObjectNode, ArrayNode> {
    private final ObjectMapper objectMapper;

    public JacksonAdapter(String mimeType, ObjectMapper objectMapper) {
        super(mimeType, ObjectNode.class, ArrayNode.class);

        this.objectMapper = objectMapper;
    }

    @Override
    public DataStructureType<SerializationAdapter<JsonNode, ObjectNode, ArrayNode>, JsonNode, ? extends JsonNode> typeOfData(String data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UniNode parse(@Nullable String data) {
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
