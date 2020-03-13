package org.comroid.uniform.data.impl.json.jackson;

import org.comroid.common.func.bi.Junction;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.StructureTypeMismatchException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.comroid.uniform.data.SeriLib.ClassDependency;

@ClassDependency({"com.fasterxml.jackson.databind.JsonNode", "com.fasterxml.jackson.databind.node.ObjectNode", "com.fasterxml.jackson.databind.node.ArrayNode"})
public final class JacksonLib extends SeriLib<JsonNode, ObjectNode, ArrayNode> {
    public static final JacksonLib jacksonLib = loadAdapter(JacksonLib.class);
    public static final ObjectMapper objectMapper = new ObjectMapper();

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

    private JacksonLib() {
        super(parser, ObjectNode.class, ArrayNode.class);
    }

    @Override
    protected <TAR extends JsonNode> NodeDummy<SeriLib<JsonNode, ObjectNode, ArrayNode>, JsonNode, ObjectNode, ArrayNode, TAR> createNodeDummy(TAR node) {
        return new NodeDummy<SeriLib<JsonNode, ObjectNode, ArrayNode>, JsonNode, ObjectNode, ArrayNode, TAR>(this, node, typeOf(node)) {
            @Override
            public boolean containsKey(String name) {
                return process(obj -> obj.has(name), arr -> {
                    throw new StructureTypeMismatchException(arrayType, objectType);
                });
            }
        };
    }
}
