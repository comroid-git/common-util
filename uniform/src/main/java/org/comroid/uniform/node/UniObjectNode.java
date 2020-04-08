package org.comroid.uniform.node;

import java.util.Map;
import java.util.stream.Stream;

import org.comroid.uniform.data.DataStructureType;
import org.comroid.uniform.data.SeriLib;

import org.jetbrains.annotations.NotNull;

public final class UniObjectNode extends UniNode {
    private final Adapter adapter;

    public UniObjectNode(SeriLib<?, ?, ?> seriLib, Adapter adapter) {
        super(seriLib, Type.OBJECT);

        this.adapter = adapter;
    }

    @Override
    public @NotNull UniNode get(String fieldName) {
        final Object value = adapter.get(fieldName);

        if (Stream.of(seriLib.objectType, seriLib.arrayType)
                .map(DataStructureType::typeClass)
                .noneMatch(type -> type.isInstance(value))) {
            return new UniValueNode<>(seriLib, makeValueAdapter(() -> (String) adapter.get(fieldName)));
        } else return seriLib.createUniNode(value);
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

    public interface Adapter extends UniNode.Adapter, Map<String, Object> {
    }
}
