package org.comroid.uniform.node;

import java.util.function.Function;

import org.comroid.common.ref.Reference;
import org.comroid.uniform.SerializationAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniValueNode<T> extends UniNode {
    public static <T> UniValueNode<T> nullNode() {
        return (UniValueNode<T>) Null.instance;
    }

    static final class Null extends UniValueNode<Void> {
        private static final UniValueNode<?> instance = new Null();

        private Null() {
            super(null, new UniValueNode.Adapter<Void>() {
                @Override
                public <R> @Nullable R get(ValueType<R> as) {
                    return null;
                }

                @Override
                public Object getBaseNode() {
                    return instance;
                }
            });
        }
    }

    public static final class ValueType<R> {
        public static final UniValueNode.ValueType<String>    STRING    =
                new ValueType<>(Function.identity());
        public static final UniValueNode.ValueType<Boolean>   BOOLEAN   =
                new ValueType<>(Boolean::parseBoolean);
        public static final UniValueNode.ValueType<Integer>   INTEGER   =
                new ValueType<>(Integer::parseInt);
        public static final UniValueNode.ValueType<Long>      LONG      =
                new ValueType<>(Long::parseLong);
        public static final UniValueNode.ValueType<Double>    DOUBLE    =
                new ValueType<>(Double::parseDouble);
        public static final UniValueNode.ValueType<Float>     FLOAT     =
                new ValueType<>(Float::parseFloat);
        public static final UniValueNode.ValueType<Short>     SHORT     =
                new ValueType<>(Short::parseShort);
        public static final UniValueNode.ValueType<Character> CHARACTER =
                new ValueType<>(str -> str.toCharArray()[0]);

        private final Function<String, R> mapper;

        public ValueType(Function<String, R> mapper) {
            this.mapper = mapper;
        }
    }

    private final Adapter<T> adapter;

    public UniValueNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter<T> adapter) {
        super(serializationAdapter, Type.VALUE);

        this.adapter = adapter;
    }

    @Override
    public final Object getBaseNode() {
        return adapter.getBaseNode();
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
        return unsupported("HAS_FIELD", Type.OBJECT);
    }

    @Override
    public @NotNull UniNode get(String fieldName) {
        return unsupported("GET_FIELD", Type.OBJECT);
    }

    @Override
    public Object asRaw(@Nullable Object fallback) {
        final String str = asString(null);

        if (str.length() == 1) {
            return asChar((char) 0);
        }

        if (str.matches("true|false")) {
            return asBoolean(false);
        }

        if (str.matches("[0-9]+")) {
            final long asLong = asLong(0);

            if (asLong > Integer.MAX_VALUE) {
                return asLong;
            } else {
                return asInt(0);
            }
        }

        if (str.matches("[0-9.]+")) {
            final double asDouble = asDouble(0);

            if (asDouble > Float.MAX_VALUE) {
                return asDouble;
            } else {
                return asFloat(0);
            }
        }

        return asString(null);
    }

    @Override
    public <R> R as(ValueType<R> type) {
        return adapter.get(type);
    }

    @Override
    public String asString(@Nullable String fallback) {
        if (isNull() && fallback != null) {
            return fallback;
        }

        return adapter.get(ValueType.STRING);
    }

    @Override
    public boolean asBoolean(boolean fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.BOOLEAN);
    }

    @Override
    public int asInt(int fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.INTEGER);
    }

    @Override
    public long asLong(long fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.LONG);
    }

    @Override
    public double asDouble(double fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.DOUBLE);
    }

    @Override
    public float asFloat(float fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.FLOAT);
    }

    @Override
    public short asShort(short fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.SHORT);
    }

    @Override
    public char asChar(char fallback) {
        if (isNull()) {
            return fallback;
        }

        return adapter.get(ValueType.CHARACTER);
    }

    public interface Adapter<T> extends UniNode.Adapter {
        final class ViaString<T> implements Adapter<T> {
            private final Reference<String> sub;

            public ViaString(Reference<String> sub) {
                this.sub = sub;
            }

            @Override
            public <R> @Nullable R get(UniValueNode.ValueType<R> as) {
                return as.mapper.apply(sub.get());
            }

            @Override
            public Object getBaseNode() {
                return null;
            }
        }

        @Nullable <R> R get(UniValueNode.ValueType<R> as);
    }
}
