package org.comroid.uniform.node;

import java.util.function.Function;

import org.comroid.common.ref.Reference;
import org.comroid.uniform.data.SeriLib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniValueNode<T> extends UniNode {
    public static <T> UniValueNode<T> nullNode() {
        return (UniValueNode<T>) Null.instance;
    }

    private final Adapter<T> adapter;

    public UniValueNode(SeriLib<?, ?, ?> seriLib, Adapter<T> adapter) {
        super(seriLib, Type.VALUE);

        this.adapter = adapter;
    }

    @Override
    public @NotNull UniNode get(String fieldName) {
        return unsupported("GET_FIELD", Type.OBJECT);
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
    public String asString(@Nullable String fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.STRING);
    }

    @Override
    public boolean asBoolean(boolean fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.BOOLEAN);
    }

    @Override
    public int asInt(int fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.INTEGER);
    }

    @Override
    public long asLong(long fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.LONG);
    }

    @Override
    public double asDouble(double fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.DOUBLE);
    }

    @Override
    public float asFloat(float fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.FLOAT);
    }

    @Override
    public short asShort(short fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.SHORT);
    }

    @Override
    public char asChar(char fallback) {
        if (isNull())
            return fallback;

        return adapter.get(Adapter.ValueType.CHARACTER);
    }

    public interface Adapter<T> extends UniNode.Adapter {
        @Nullable <R> R get(ValueType<R> as);

        final class ValueType<T> {
            public static final ValueType<String> STRING = new ValueType<>(Function.identity());
            public static final ValueType<Boolean> BOOLEAN = new ValueType<>(Boolean::parseBoolean);
            public static final ValueType<Integer> INTEGER = new ValueType<>(Integer::parseInt);
            public static final ValueType<Long> LONG = new ValueType<>(Long::parseLong);
            public static final ValueType<Double> DOUBLE = new ValueType<>(Double::parseDouble);
            public static final ValueType<Float> FLOAT = new ValueType<>(Float::parseFloat);
            public static final ValueType<Short> SHORT = new ValueType<>(Short::parseShort);
            public static final ValueType<Character> CHARACTER = new ValueType<>(str -> str.toCharArray()[0]);

            private final Function<String, T> mapper;

            public ValueType(Function<String, T> mapper) {
                this.mapper = mapper;
            }
        }

        final class ViaString<T> implements Adapter<T> {
            private final Reference<String> sub;

            public ViaString(Reference<String> sub) {
                this.sub = sub;
            }

            @Override
            public <R> @Nullable R get(ValueType<R> as) {
                return as.mapper.apply(sub.get());
            }
        }
    }

    static final class Null extends UniValueNode<Void> {
        private Null() {
            super(null, null);
        }

        private static final UniValueNode<?> instance = new Null();
    }
}
