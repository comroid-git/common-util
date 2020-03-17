package org.comroid.uniform.data.util.json;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;

import static org.comroid.common.Polyfill.deadCast;

public class JacksonUtils {
    public static <T> T nodeAs(JsonNode jsonNode, Class<T> targetType) {
        if ((String.class.isAssignableFrom(targetType) || Object.class.equals(targetType))
                && jsonNode instanceof TextNode)
            return deadCast(jsonNode.asText());

        if ((Boolean.class.isAssignableFrom(targetType) || Object.class.equals(targetType))
                && jsonNode instanceof BooleanNode)
            return deadCast(jsonNode.asBoolean());

        if (Number.class.isAssignableFrom(targetType) && jsonNode instanceof NumericNode) {
            if (Objects.class.equals(targetType))
                return deadCast(jsonNode.asDouble());

            if (Integer.class.isAssignableFrom(targetType))
                return deadCast(jsonNode.asInt());
            if (Long.class.isAssignableFrom(targetType))
                return deadCast(jsonNode.asLong());
            if (Double.class.isAssignableFrom(targetType) || Float.class.isAssignableFrom(targetType))
                return deadCast(jsonNode.asDouble());
        }

        throw new AssertionError();
    }
}
