package org.comroid.uniform.data.impl.json.jackson;

import java.util.ArrayList;
import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataConverter$Jackson<T> extends DataConverter<T, JsonNode, ObjectNode, ArrayNode> {
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


    public static <T> Junction<ObjectNode, T> autoConverter(Class<T> forClass) {
        return new Junction<ObjectNode, T>() {
            private final Class<T> target = forClass;

            @Override
            public T forward(ObjectNode node) {
                try {
                    return objectMapper.readValue(node.toString(), target);
                } catch (JsonProcessingException e) {
                    throw new AssertionError("Unexpected JsonProcessingException", e);
                }
            }

            @Override
            public ObjectNode backward(T object) {
                return objectMapper.valueToTree(object);
            }
        };
    }

    private final PredicateDuo<ObjectNode, T> filter;
    private final Junction<ObjectNode, T> converter;

    public DataConverter$Jackson(PredicateDuo<ObjectNode, T> filter, Junction<ObjectNode, T> converter) {
        super(JacksonLib.instance, "application/json");

        this.filter = filter;
        this.converter = converter;
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
    public Collection<JsonNode> split(ArrayNode data) {
        final ArrayList<JsonNode> yields = new ArrayList<>();

        data.forEach(yields::add);

        return yields;
    }

    @Override
    public ArrayNode combine(Span<JsonNode> data) {
        final ArrayNode yields = JsonNodeFactory.instance.arrayNode(data.size());

        yields.addAll(data);

        return yields;
    }
}
