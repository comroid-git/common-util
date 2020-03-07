package org.comroid.common.rest.adapter.data.json;

import java.util.ArrayList;
import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;
import org.comroid.common.rest.adapter.data.DataConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataConverter$Jackson<T> implements DataConverter<T, JsonNode, ObjectNode, ArrayNode> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Junction<String, JsonNode> parser = Junction.of(
            str -> {
                try {
                    return objectMapper.readTree(str);
                } catch (JsonProcessingException e) {
                    throw new AssertionError(e);
                }
            },
            JsonNode::toPrettyString
    );

    private final PredicateDuo<ObjectNode, T> filter;
    private final Junction<ObjectNode, T> converter;

    public DataConverter$Jackson(PredicateDuo<ObjectNode, T> filter, Junction<ObjectNode, T> converter) {
        this.filter = filter;
        this.converter = converter;
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public Junction<String, JsonNode> getParser() {
        return parser;
    }

    @Override
    public PredicateDuo<ObjectNode, T> getFilter() {
        return filter;
    }

    @Override
    public Junction<ObjectNode, T> getConverter() {
        return converter;
    }

    @Override
    public DataStructureType getStructureType(JsonNode data) {
        return data.isArray() ? DataStructureType.ARRAY : DataStructureType.OBJECT;
    }

    @Override
    public Collection<ObjectNode> split(ArrayNode data) {
        final ArrayList<ObjectNode> yields = new ArrayList<>();

        data.forEach(it -> yields.add((ObjectNode) it));

        return yields;
    }

    @Override
    public ArrayNode combine(Span<ObjectNode> data) {
        final ArrayNode yields = JsonNodeFactory.instance.arrayNode(data.size());

        yields.addAll(data);

        return yields;
    }
}
