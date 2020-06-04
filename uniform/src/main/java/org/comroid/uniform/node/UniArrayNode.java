package org.comroid.uniform.node;

import org.comroid.common.func.Junction;
import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.OutdateableReference.SettableOfSupplier;
import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class UniArrayNode extends UniNode {
    private final Map<Integer, UniValueNode<String>> valueAdapters
            = new TrieMap.Basic<>(Junction.of(String::valueOf, Integer::parseInt), true);
    private final Adapter adapter;

    @Override
    public final Object getBaseNode() {
        return adapter.getBaseNode();
    }

    public UniArrayNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter adapter) {
        super(serializationAdapter, Type.ARRAY);

        this.adapter = adapter;
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
            return valueAdapters.computeIfAbsent(index, k -> generateValueNode(new SettableOfSupplier<>(() -> value)
                    .process()
                    .map(String::valueOf)
                    .snapshot()));
        } else {
            return serializationAdapter.createUniNode(value);
        }
    }

    @Override
    public UniArrayNode copyFrom(@NotNull UniNode it) {
        if (it instanceof UniArrayNode) {
            //noinspection unchecked
            adapter.addAll(((UniArrayNode) it).adapter);
            return this;
        }
        return unsupported("COPY_FROM", Type.ARRAY);
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
    public @NotNull <T> UniValueNode<String> put(int index, ValueType<T> type, T value) {
        if (adapter.size() > index) {
            Object at = adapter.get(index);
            if (at instanceof UniValueNode) {
                adapter.set(index, value);
                //noinspection unchecked
                return (UniValueNode<String>) at;
            }
        }

        adapter.set(index, value);
        return valueAdapters.computeIfAbsent(index, k -> generateValueNode(new SettableOfSupplier<>(() -> value)
                .process()
                .map(String::valueOf)
                .snapshot()));
    }

    @Override
    public @NotNull UniObjectNode putObject(int index) {
        final UniObjectNode objectNode = serializationAdapter.createUniObjectNode();

        adapter.add(index, objectNode.getBaseNode());
        return objectNode;
    }

    @Override
    public @NotNull UniArrayNode putArray(int index) {
        final UniArrayNode arrayNode = serializationAdapter.createUniArrayNode();

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

    public static abstract class Adapter<B> extends AbstractList<Object> implements UniNode.Adapter<B> {
        protected final B baseNode;

        @Override
        public B getBaseNode() {
            return baseNode;
        }

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
    }
}
