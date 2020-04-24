package org.comroid.uniform.node;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.SerializationAdapter;

import org.jetbrains.annotations.NotNull;

public final class UniObjectNode extends UniNode {
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

    public static abstract class Adapter<B> extends AbstractMap<String, Object> implements UniNode.Adapter<B> {
        protected final B baseNode;

        protected Adapter(B baseNode) {
            this.baseNode = baseNode;
        }

        @Override
        public abstract Object put(String key, Object value);

        @NotNull
        @Override
        public abstract Set<Entry<String, Object>> entrySet();

        @Override
        public B getBaseNode() {
            return baseNode;
        }
    }
    private final Adapter adapter;

    public UniObjectNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter adapter) {
        super(serializationAdapter, Type.OBJECT);

        this.adapter = adapter;
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
}
