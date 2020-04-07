package org.comroid.uniform.node;

import java.util.Map;

public final class UniObjectNode extends UniNode {
    private final Adapter objectAdapter;

    public UniObjectNode(API API, Adapter objectAdapter) {
        super(API, Type.OBJECT);

        this.objectAdapter = objectAdapter;
    }

    public final UniObjectNode getObject(String fieldName) {
    }

    public final UniArrayNode getArray(String fieldName) {
        return new UniArrayNode();
    }

    public final <T> UniValueNode<T> get(String fieldName, Class<T> asType) {
        return API.objectAccessingValueNode(identifier, fieldName, asType);
    }

    public interface Adapter extends Map<String, Object> {}
}
