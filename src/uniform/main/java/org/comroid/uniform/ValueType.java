package org.comroid.uniform;

import java.util.function.Function;

public final class ValueType<R> implements HeldType<R> {
    public static final ValueType<Boolean> BOOLEAN = new ValueType<>(Boolean::parseBoolean);
    public static final ValueType<Character> CHARACTER = new ValueType<>(str -> str.toCharArray()[0]);
    public static final ValueType<Double> DOUBLE = new ValueType<>(Double::parseDouble);
    public static final ValueType<Float> FLOAT = new ValueType<>(Float::parseFloat);
    public static final ValueType<Integer> INTEGER = new ValueType<>(Integer::parseInt);
    public static final ValueType<Long> LONG = new ValueType<>(Long::parseLong);
    public static final ValueType<Short> SHORT = new ValueType<>(Short::parseShort);
    public static final ValueType<String> STRING = new ValueType<>(Function.identity());
    public static final ValueType<Void> VOID = new ValueType<>(it -> null);

    private final Function<String, R> mapper;

    @Deprecated
    public Function<String, R> getMapper() {
        return this;
    }

    public ValueType(Function<String, R> mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T convert(R value, ValueType<T> toType) {
        return toType.apply(value.toString());
    }

    @Override
    public R apply(String from) {
        return mapper.apply(from);
    }
}
