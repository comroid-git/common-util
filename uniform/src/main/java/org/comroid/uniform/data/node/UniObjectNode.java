package org.comroid.uniform.data.node;

import java.util.Map;
import java.util.stream.Stream;

import org.comroid.uniform.data.DataStructureType;
import org.comroid.uniform.data.SerializationAdapter;

import org.jetbrains.annotations.NotNull;

public final class UniObjectNode extends UniNode {
    private final Adapter adapter;

    public UniObjectNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter adapter) {
        super(serializationAdapter, Type.OBJECT);

        this.adapter = adapter;
    }

    @Override
    public @NotNull UniNode get(String fieldName) {
        final Object value = adapter.get(fieldName);

        if (value == null)
            return UniValueNode.nullNode();

        if (Stream.of(serializationAdapter.objectType, serializationAdapter.arrayType)
                .map(DataStructureType::typeClass)
                .noneMatch(type -> type.isInstance(value))) {
            return new UniValueNode<>(serializationAdapter, makeValueAdapter(() -> String.valueOf(adapter.get(fieldName))));
        } else return serializationAdapter.createUniNode(value);
    }

    @Override
    public @NotNull UniNode get(int index) {
        return unsupported("GET_INDEX", Type.ARRAY);
    }

    @Override
    public int size() {
        return unsupported("SIZE", Type.ARRAY);
    }

    @Override
    public boolean has(String fieldName) {
        return adapter.containsKey(fieldName);
    }

    @Override
    public final Object getBaseNode() {
        return adapter.getBaseNode();
    }

    public interface Adapter extends UniNode.Adapter, Map<String, Object> {
    }
}
