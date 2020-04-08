package org.comroid.uniform.data.impl.json.jackson;

import java.util.ArrayList;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.annotation.Instance;
import org.comroid.common.func.bi.Junction;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniObjectNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ClassDependency({
        "com.fasterxml.jackson.databind.JsonNode",
        "com.fasterxml.jackson.databind.node.ObjectNode",
        "com.fasterxml.jackson.databind.node.ArrayNode"
})
public final class JacksonLib extends SeriLib<JsonNode, ObjectNode, ArrayNode> {
    public static @Instance final JacksonLib jacksonLib = new JacksonLib();
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private JacksonLib() {
        super(
                parser, ObjectNode.class, ArrayNode.class,
                (jsonNodes, s) -> ((ArrayNode) jsonNodes.get(s)), ArrayNode::size, array -> {
                    final ArrayList<JsonNode> objects = new ArrayList<>();
                    array.forEach(objects::add);
                    return objects;
                }
        );
    }

    @Override
    public <MT> UniObjectNode<JsonNode, ObjectNode, MT> createUniObjectNode(
            ObjectNode node
    ) {
        throw new UnsupportedOperationException("no class defined");
    }

    @Override
    public <CT> UniArrayNode<JsonNode, ArrayNode, CT> createUniArrayNode(
            ArrayNode node
    ) {
        throw new UnsupportedOperationException("no class defined");
    }
    private static final Junction<String, JsonNode> parser = Junction.of(str -> {
        try {
            return objectMapper.readTree(str);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }, JsonNode::toPrettyString);
}
