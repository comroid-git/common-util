package org.comroid.uniform.node;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.ref.Reference;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class UniArrayNode extends UniNode {
    private final Map<Integer, UniValueNode<String>> valueAdapters = new ConcurrentHashMap<>();
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
        return makeValueNode(index).orElseGet(UniValueNode::nullNode);
    }

    private Processor<UniNode> makeValueNode(int index) {
        class Accessor implements Reference.Settable<String> {
            private final int index;

            private Accessor(int index) {
                this.index = index;
            }

            @Nullable
            @Override
            public String get() {
                return unwrapDST(adapter.get(index));
            }

            @Nullable
            @Override
            public String set(String newValue) {
                adapter.set(index, newValue);
                return get();
            }
        }

        String key = String.valueOf(index);
        return computeNode(key, () -> new Accessor(index));
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
    public @NotNull <T> UniNode put(int index, HeldType<T> type, T value) {
        UniNode node = unwrapNode(String.valueOf(index), type, value);
        if (node != null)
            return node;

        if (type == ValueType.VOID) {
            adapter.set(index, value);
            return get(index);
        } else {
            final String put = type.convert(value, ValueType.STRING);

            return makeValueNode(index)
                    .peek(newNode -> newNode.set(put))
                    .requireNonNull("Missing Node");
        }
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
