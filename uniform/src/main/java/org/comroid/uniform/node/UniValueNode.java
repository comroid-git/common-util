package org.comroid.uniform.node;

import org.comroid.common.ref.Reference;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniValueNode<T> extends UniNode {
    private final Adapter<T> adapter;

    @Override
    public final Object getBaseNode() {
        return asRaw(null);
    }

    public UniValueNode(SerializationAdapter<?, ?, ?> serializationAdapter, Adapter<T> adapter) {
        super(serializationAdapter, Type.VALUE);

        this.adapter = adapter;
    }

    public static <T> UniValueNode<T> nullNode() {
        return (UniValueNode<T>) Null.instance;
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
    public UniValueNode copyFrom(@NotNull UniNode it) {
        if (it instanceof UniValueNode) {
            this.adapter.set(it.asString(null));
            return this;
        }
        return unsupported("COPY_FROM", Type.VALUE);
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
        @Nullable <R> R get(ValueType<R> as);

        @Nullable String set(String value);

        final class ViaString implements Adapter<String> {
            private final Reference.Settable<String> sub;

            @Override
            public Object getBaseNode() {
                return null;
            }

            public ViaString(Reference.Settable<String> sub) {
                this.sub = sub;
            }

            @Override
            public <R> @Nullable R get(ValueType<R> as) {
                return as.apply(sub.get());
            }

            @Override
            public @Nullable String set(String value) {
                return sub.set(value);
            }

            @Override
            public String toString() {
                return sub.get();
            }
        }
    }

    static final class Null extends UniValueNode<Void> {
        private static final UniValueNode<?> instance = new Null();

        private Null() {
            super(null, new UniValueNode.Adapter<Void>() {
                @Override
                public Object getBaseNode() {
                    return instance;
                }

                @Override
                public <R> @Nullable R get(ValueType<R> as) {
                    return null;
                }

                @Override
                public @Nullable String set(String value) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String toString() {
                    return "null";
                }
            });
        }
    }
}
