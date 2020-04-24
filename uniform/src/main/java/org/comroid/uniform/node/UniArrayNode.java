package org.comroid.uniform.node;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;

import org.jetbrains.annotations.NotNull;

public final class UniArrayNode extends UniNode {
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

    public static UniArrayNode ofList(SerializationAdapter<?, ?, ?> adapter, List<Object> list) {
        class MergedAdapter extends Adapter<List<Object>> {
            protected MergedAdapter(List<Object> underlying) {
                super(underlying);
            }

            @Override
            public int size() {
                return getBaseNode().size();
            }

            @Override
            public Object get(int index) {
                return getBaseNode().get(index);
            }

            @Override
            public Object set(int index, Object element) {
                return getBaseNode().set(index, element);
            }

            @Override
            public void add(int index, Object element) {
                getBaseNode().add(index, element);
            }

            @Override
            public Object remove(int index) {
                return getBaseNode().remove(index);
            }
        }

        return new UniArrayNode(adapter, new MergedAdapter(list));
    }

    public static abstract class Adapter<B> extends AbstractList<Object> implements UniNode.Adapter<B> {
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
        protected final B baseNode;
    }
    private final Adapter adapter;
}
