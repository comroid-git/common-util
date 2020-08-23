package org.comroid.uniform.node;

import org.comroid.mutatio.ref.Reference;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniValueNode<T> extends UniNode {
    private final Reference<T> baseReference;
    private final ValueType<T> targetType;

    public UniValueNode(SerializationAdapter<?, ?, ?> seriLib, Reference<T> baseReference, ValueType<T> targetType) {
        super(seriLib, Type.VALUE);
        this.baseReference = baseReference;
        this.targetType = targetType;
    }

    public static <T> UniValueNode<T> nullNode() {
        return (UniValueNode<T>) Null.instance;
    }

    @Override
    public @NotNull UniNode get(int index) {
        return unsupported("GET_INDEX", Type.ARRAY);
    }

    @Override
    public Object getBaseNode() {
        return baseReference.get();
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
    protected void set(Object value) {
        baseReference.set(
                ValueType.STRING.convert(
                        String.valueOf(value), targetType)
        );
    }

    @Override
    public UniValueNode<T> copyFrom(@NotNull UniNode it) {
        if (it instanceof UniValueNode) {
            baseReference.set(it.as(targetType));
            return this;
        }
        return unsupported("COPY_FROM_" + it.getType().name(), Type.VALUE);
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
        return baseReference.into(it -> targetType.convert(it, type));
    }

    @Override
    public String asString(@Nullable String fallback) {
        if (isNull() && fallback != null) {
            return fallback;
        }

        return as(ValueType.STRING);
    }

    @Override
    public boolean asBoolean(boolean fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.BOOLEAN);
    }

    @Override
    public int asInt(int fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.INTEGER);
    }

    @Override
    public long asLong(long fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.LONG);
    }

    @Override
    public double asDouble(double fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.DOUBLE);
    }

    @Override
    public float asFloat(float fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.FLOAT);
    }

    @Override
    public short asShort(short fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.SHORT);
    }

    @Override
    public char asChar(char fallback) {
        if (isNull()) {
            return fallback;
        }

        return as(ValueType.CHARACTER);
    }

    public interface Adapter<T> extends UniNode.Adapter {
        @Nullable <R> R get(ValueType<R> as);

        @Nullable String set(String value);

        final class ViaString implements Adapter<String> {
            private final Reference<String> sub;

            @Override
            public Object getBaseNode() {
                return null;
            }

            public ViaString(Reference<String> sub) {
                this.sub = sub;
            }

            @Override
            public <R> @Nullable R get(ValueType<R> as) {
                final String from = sub.get();
                if (from != null)
                    return as.getConverter().forward(from);
                return null;
            }

            @Override
            public @Nullable String set(String value) {
                sub.set(value);
                return null;
            }

            @Override
            public String toString() {
                return String.format("\"%s\"", sub.get());
            }
        }
    }

    static final class Null extends UniValueNode<Void> {
        private static final UniValueNode<?> instance = new Null();

        private Null() {
            super(null, Reference.empty(), ValueType.VOID);
        }
    }
}
