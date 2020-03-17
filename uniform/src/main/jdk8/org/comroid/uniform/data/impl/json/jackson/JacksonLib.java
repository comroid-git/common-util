package org.comroid.uniform.data.impl.json.jackson;

import java.util.ArrayList;
import java.util.function.BiFunction;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.annotation.Instance;
import org.comroid.common.func.bi.Junction;
import org.comroid.common.iter.Span;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.StructureTypeMismatchException;
import org.comroid.uniform.data.util.json.JacksonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ClassDependency({"com.fasterxml.jackson.databind.JsonNode", "com.fasterxml.jackson.databind.node.ObjectNode", "com.fasterxml.jackson.databind.node.ArrayNode"})
public final class JacksonLib extends SeriLib<JsonNode, ObjectNode, ArrayNode> {
    public static @Instance final JacksonLib jacksonLib = new JacksonLib();
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
        super(parser, ObjectNode.class, ArrayNode.class, (jsonNodes, s) -> ((ArrayNode) jsonNodes.get(s)), ArrayNode::size, array -> {
            final ArrayList<JsonNode> objects = new ArrayList<>();
            array.forEach(objects::add);
            return objects;
        });

        ReflectionHelper.verifyClassDependencies(JacksonLib.class);
    }

    @Override
    protected <TAR extends JsonNode> NodeDummy<SeriLib<JsonNode, ObjectNode, ArrayNode>, JsonNode, ObjectNode, ArrayNode, TAR> createNodeDummy(TAR node) {
        return new NodeDummy<SeriLib<JsonNode, ObjectNode, ArrayNode>, JsonNode, ObjectNode, ArrayNode, TAR>(this, node) {
            @Override
            public boolean containsKey(String name) {
                return process(obj -> obj.has(name), arr -> {
                    throw new StructureTypeMismatchException(arrayType, objectType);
                });
            }

            @Override
            public <T> T getValueAs(final String fieldName, final Class<T> targetType) {
                return process(obj -> JacksonUtils.nodeAs(obj.get(fieldName), targetType), arr -> {
                    // todo
                });
            }
        };
    }
}
