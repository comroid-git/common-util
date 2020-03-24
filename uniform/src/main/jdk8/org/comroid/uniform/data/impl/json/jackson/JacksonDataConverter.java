package org.comroid.uniform.data.impl.json.jackson;

import java.util.ArrayList;
import java.util.Collection;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.func.bi.PredicateDuo;
import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonDataConverter<T> extends DataConverter<T, JsonNode, ObjectNode, ArrayNode> {
    public static <T> Junction<ObjectNode, T> autoConverter(Class<T> forClass) {
        return new Junction<ObjectNode, T>() {
            private final Class<T> target = forClass;

            @Override
            public T forward(ObjectNode node) {
                try {
                    return JacksonLib.objectMapper.readValue(node.toString(), target);
                } catch (JsonProcessingException e) {
                    throw new AssertionError("Unexpected JsonProcessingException", e);
                }
            }

            @Override
            public ObjectNode backward(T object) {
                return JacksonLib.objectMapper.valueToTree(object);
            }
        };
    }

    private final PredicateDuo<ObjectNode, T> filter;
    private final Junction<ObjectNode, T>     converter;

    public JacksonDataConverter(
            PredicateDuo<ObjectNode, T> filter,
            Junction<ObjectNode, T> converter
    ) {
        super(JacksonLib.jacksonLib, "application/json");

        this.filter = filter;
        this.converter = converter;
    }

    @Override
    public PredicateDuo<ObjectNode, T> getFilter() {
        return filter;
    }

    @Override
    public Collection<JsonNode> split(ArrayNode data) {
        final ArrayList<JsonNode> yields = new ArrayList<>();

        data.forEach(yields::add);

        return yields;
    }

    @Override
    public Junction<ObjectNode, T> getConverter() {
        return converter;
    }

    @Override
    public ArrayNode combine(Span<JsonNode> data) {
        final ArrayNode yields = JsonNodeFactory.instance.arrayNode(data.size());

        yields.addAll(data);

        return yields;
    }
}
