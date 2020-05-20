package org.comroid.uniform.node;

import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class UniObjectNode extends UniNode {
    private final Adapter adapter;

    @Override
    public final Object getBaseNode() {
        return adapter.getBaseNode();
    }

    public UniObjectNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter adapter) {
        super(serializationAdapter, Type.OBJECT);

        this.adapter = adapter;
    }

    public static UniObjectNode ofMap(SerializationAdapter<?, ?, ?> adapter, Map<String, Object> map) {
        class MergedAdapter extends UniObjectNode.Adapter<Map<String, Object>> {
            protected MergedAdapter(Map<String, Object> underlying) {
                super(underlying);
            }

            @Override
            public Object put(String key, Object value) {
                return getBaseNode().put(key, value);
            }

            @Override
            public @NotNull Set<Entry<String, Object>> entrySet() {
                return getBaseNode().entrySet();
            }
        }

        return new UniObjectNode(adapter, new MergedAdapter(map));
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
    public @NotNull UniNode get(String fieldName) {
        final Object value = adapter.get(fieldName);

        if (value == null) {
            return UniValueNode.nullNode();
        }

        if (Stream.of(serializationAdapter.objectType, serializationAdapter.arrayType)
                .map(DataStructureType::typeClass)
                .noneMatch(type -> type.isInstance(value))) {
            return new UniValueNode<>(serializationAdapter, makeValueAdapter(() -> String.valueOf(adapter.get(fieldName))));
        } else {
            return serializationAdapter.createUniNode(value);
        }
    }

    @Override
    public @NotNull <T> UniValueNode<T> put(String key, ValueType<T> type, T value) {
        final UniValueNode<T> valueNode = generateValueNode(type.convert(value, ValueType.STRING));

        adapter.put(key, valueNode.getBaseNode());
        return valueNode;
    }

    @Override
    public UniNode putNull(String key) throws UnsupportedOperationException {
        final UniNode nullNode = serializationAdapter.createUniNode(null);

        adapter.put(key, nullNode);
        return nullNode;
    }

    @Override
    public @NotNull UniObjectNode putObject(String key) {
        final UniObjectNode objectNode = serializationAdapter.createUniObjectNode(null);

        adapter.put(key, objectNode.getBaseNode());
        return objectNode;
    }

    @Override
    public @NotNull UniArrayNode putArray(String key) {
        final UniArrayNode arrayNode = serializationAdapter.createUniArrayNode(null);

        adapter.put(key, arrayNode.getBaseNode());
        return arrayNode;
    }

    @Override
    public String toString() {
        return adapter.toString();
    }

    public static abstract class Adapter<B> extends AbstractMap<String, Object> implements UniNode.Adapter<B> {
        protected final B baseNode;

        @Override
        public B getBaseNode() {
            return baseNode;
        }

        protected Adapter(B baseNode) {
            this.baseNode = baseNode;
        }

        @Override
        public abstract Object put(String key, Object value);

        @NotNull
        @Override
        public abstract Set<Entry<String, Object>> entrySet();
    }
}
