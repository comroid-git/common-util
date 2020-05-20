package org.comroid.uniform.node;

import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.comroid.common.Polyfill.uncheckedCast;

public final class UniArrayNode extends UniNode {
    private final Adapter adapter;

    public UniArrayNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter adapter) {
        super(serializationAdapter, Type.ARRAY);

        this.adapter = adapter;
    }

    @Override
    public final Object getBaseNode() {
        return adapter.getBaseNode();
    }

    @Override
    public @NotNull UniNode get(int index) {
        final Object value = adapter.get(index);

        if (value == null) {
            return UniValueNode.nullNode();
        }

        if (value instanceof UniNode) {
            return (UniNode) value;
        }

        if (Stream.of(serializationAdapter.objectType, serializationAdapter.arrayType)
                .map(DataStructureType::typeClass)
                .noneMatch(type -> type.isInstance(value))) {
            return new UniValueNode<>(serializationAdapter, makeValueAdapter(() -> String.valueOf(adapter.get(index))));
        } else {
            return serializationAdapter.createUniNode(value);
        }
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
    public @NotNull UniNode get(String fieldName) {
        return unsupported("GET_FIELD", Type.OBJECT);
    }

    @Override
    public @NotNull <T> UniValueNode<T> put(int index, ValueType<T> type, T value) {
        final UniValueNode<T> valueNode = generateValueNode(type.convert(value, ValueType.STRING));

        adapter.add(index, valueNode.getBaseNode());
        return valueNode;
    }

    @Override
    public @NotNull UniObjectNode putObject(int index) {
        final UniObjectNode objectNode = serializationAdapter.createUniObjectNode(null);

        adapter.add(index, objectNode.getBaseNode());
        return objectNode;
    }

    @Override
    public @NotNull UniArrayNode putArray(int index) {
        final UniArrayNode arrayNode = serializationAdapter.createUniArrayNode(null);

        adapter.add(index, arrayNode.getBaseNode());
        return arrayNode;
    }

    @Override
    public synchronized List<Object> asList() {
        final List<Object> yields = new ArrayList<>();

        for (int i = 0; i < size(); i++) {
            yields.add(get(i).asRaw(null));
        }

        return yields;
    }

    @Override
    public synchronized List<? extends UniNode> asNodeList() {
        final List<UniNode> yields = new ArrayList<>();

        for (int i = 0; i < size(); i++) {
            yields.add(get(i));
        }

        return yields;
    }

    @Override
    public String toString() {
        return adapter.toString();
    }

    public static abstract class Adapter<B> extends AbstractList<Object> implements UniNode.Adapter<B> {
        protected final B baseNode;

        protected Adapter(B baseNode) {
            this.baseNode = baseNode;
        }

        @Override
        public abstract Object get(int index);

        @Override
        public abstract Object set(int index, Object element);

        @Override
        public abstract void add(int index, Object element);

        @Override
        public abstract Object remove(int index);

        @Override
        public B getBaseNode() {
            return baseNode;
        }
    }
}
