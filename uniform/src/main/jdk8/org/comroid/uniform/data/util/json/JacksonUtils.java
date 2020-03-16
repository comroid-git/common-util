package org.comroid.uniform.data.util.json;

import com.fasterxml.jackson.databind.JsonNode;

import static org.comroid.common.Polyfill.deadCast;

public class JacksonUtils {
    public static <T> T nodeAs(JsonNode jsonNode, Class<T> targetType) {
        if (String.class.isAssignableFrom(targetType))
            return deadCast(jsonNode.asText());

        if (Boolean.class.isAssignableFrom(targetType))
            return deadCast(jsonNode.asBoolean());

        if (Number.class.isAssignableFrom(targetType)) {
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
