package org.comroid.uniform;

import java.io.Serializable;
import java.util.function.Function;

public final class ValueType<R extends Serializable> implements Function<String, R> {
    public static final ValueType<Boolean> BOOLEAN = new ValueType<>(Boolean::parseBoolean);
    public static final ValueType<Character> CHARACTER = new ValueType<>(str -> str.toCharArray()[0]);
    public static final ValueType<Double> DOUBLE = new ValueType<>(Double::parseDouble);
    public static final ValueType<Float> FLOAT = new ValueType<>(Float::parseFloat);
    public static final ValueType<Integer> INTEGER = new ValueType<>(Integer::parseInt);
    public static final ValueType<Long> LONG = new ValueType<>(Long::parseLong);
    public static final ValueType<Short> SHORT = new ValueType<>(Short::parseShort);
    public static final ValueType<String> STRING = new ValueType<>(Function.identity());

    private final Function<String, R> mapper;

    public Function<String, R> getMapper() {
        return mapper;
    }

    public ValueType(Function<String, R> mapper) {
        this.mapper = mapper;
    }

    public <T extends Serializable> T convert(R value, ValueType<T> toType) {
        return toType.apply(value.toString());
    }

    @Override
    public R apply(String from) {
        return mapper.apply(from);
    }
}
