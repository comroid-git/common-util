package org.comroid.uniform.adapter.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.comroid.common.exception.AssertionException;
import org.comroid.mutatio.ref.Reference;
import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class JacksonAdapter extends SerializationAdapter<JsonNode, ObjectNode, ArrayNode> {
    private final ObjectMapper objectMapper;

    public JacksonAdapter(String mimeType, ObjectMapper objectMapper) {
        super(mimeType, ObjectNode.class, JsonNodeFactory.instance::objectNode, ArrayNode.class, JsonNodeFactory.instance::arrayNode);

        this.objectMapper = objectMapper;
    }

    @Override
    public DataStructureType<SerializationAdapter<JsonNode, ObjectNode, ArrayNode>, JsonNode, ? extends JsonNode> typeOfData(String data) {
        try {
            final JsonNode node = objectMapper.readTree(data);

            if (node.isArray())
                return arrayType;
            if (node.isObject())
                return objectType;
            if (node.isValueNode())
                return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Invalid %s data: \n%s", mimeType, data), e);
        }

        throw new AssertionException();
    }

    @Override
    public UniNode parse(@Nullable String data) {
        try {
            final JsonNode node = objectMapper.readTree(data);

            if (node.isArray())
                return createUniArrayNode((ArrayNode) node);
            if (node.isObject())
                return createUniObjectNode((ObjectNode) node);
            if (node.isValueNode())
                return createValueNode((ValueNode) node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Invalid %s data: \n%s", mimeType, data), e);
        }

        throw new AssertionException();
    }

    private UniValueNode<String> createValueNode(ValueNode dataString) {
        return new UniValueNode<>(this, new UniValueNode.Adapter.ViaString(new Reference.Support.Base<String>(false) {
            private final ValueNode base = dataString;

            @Override
            protected String doGet() {
                return base.asText();
            }
        }));
    }

    @Override
    public UniObjectNode createUniObjectNode(ObjectNode node) {
        return new UniObjectNode(this, new UniObjectNode.Adapter<ObjectNode>(node) {
            @Override
            public Object put(String key, Object value) {
                return baseNode.put(key, String.valueOf(value));
            }

            @Override
            public @NotNull Set<Entry<String, Object>> entrySet() {
                final Iterator<String> keys = baseNode.fieldNames();
                final Set<Entry<String, Object>> yield = new HashSet<>();

                while (keys.hasNext()) {
                    final String key = keys.next();

                    yield.add(new SimpleImmutableEntry<>(key, baseNode.get(key)));
                }

                return yield;
            }
        });
    }

    @Override
    public UniArrayNode createUniArrayNode(ArrayNode node) {
        throw new UnsupportedOperationException();
    }
}
