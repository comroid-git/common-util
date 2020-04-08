package org.comroid.uniform.data.node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.comroid.uniform.data.DataStructureType;
import org.comroid.uniform.data.SerializationAdapter;

import org.jetbrains.annotations.NotNull;

public final class UniArrayNode extends UniNode {
    private final Adapter adapter;

    public UniArrayNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter adapter) {
        super(serializationAdapter, Type.ARRAY);

        this.adapter = adapter;
    }

    @Override
    public @NotNull UniNode get(String fieldName) {
        return unsupported("GET_FIELD", Type.OBJECT);
    }

    @Override
    public @NotNull UniNode get(int index) {
        final Object value = adapter.get(index);

        if (value == null)
            return UniValueNode.nullNode();

        if (Stream.of(serializationAdapter.objectType, serializationAdapter.arrayType)
                .map(DataStructureType::typeClass)
                .noneMatch(type -> type.isInstance(value))) {
            return new UniValueNode<>(serializationAdapter, makeValueAdapter(() -> (String) adapter.get(index)));
        } else return serializationAdapter.createUniNode(value);
    }

    @Override
    public int size() {
        return adapter.size();
    }

    @Override
    public boolean has(String fieldName) {
        return unsupported("HAS_FIELD", Type.OBJECT);
    }

    @Override
    public synchronized List<Object> asList() {
        final List<Object> yields = new ArrayList<>();

        for (int i = 0; i < size(); i++) {
            yields.set(i, get(i).asRaw(null));
        }

        return yields;
    }

    @Override
    public List<? extends UniNode> asNodeList() {
        final List<UniNode> yields = new ArrayList<>();

        for (int i = 0; i < size(); i++) {
            yields.set(i, get(i));
        }

        return yields;
    }

    @Override
    public final Object getBaseNode() {
        return adapter.getBaseNode();
    }

    public interface Adapter extends UniNode.Adapter, List<Object> {
    }
}
