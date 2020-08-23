package org.comroid.uniform;

import org.comroid.api.Junction;

import java.util.function.Function;

public final class ValueType<R> implements HeldType<R> {
    public static final ValueType<Boolean> BOOLEAN
            = new ValueType<>("boolean", Boolean::parseBoolean);
    public static final ValueType<Character> CHARACTER
            = new ValueType<>("char", str -> str.toCharArray()[0]);
    public static final ValueType<Double> DOUBLE
            = new ValueType<>("double", Double::parseDouble);
    public static final ValueType<Float> FLOAT
            = new ValueType<>("float", Float::parseFloat);
    public static final ValueType<Integer> INTEGER
            = new ValueType<>("int", Integer::parseInt);
    public static final ValueType<Long> LONG
            = new ValueType<>("long", Long::parseLong);
    public static final ValueType<Short> SHORT
            = new ValueType<>("short", Short::parseShort);
    public static final ValueType<String> STRING
            = new ValueType<>("String", Function.identity());

    public static final ValueType<Void> VOID
            = new ValueType<>("Void", it -> null);

    private final String name;
    private final Junction<String, R> converter;

    @Deprecated
    public Function<String, R> getMapper() {
        return getConverter()::forward;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Junction<String, R> getConverter() {
        return converter;
    }

    public ValueType(String name, Function<String, R> mapper) {
        this.name = name;
        this.converter = Junction.ofString(mapper);
    }

    @Override
    public <T> T convert(R value, HeldType<T> toType) {
        return toType.getConverter().forward(value.toString());
    }

    @Override
    public String toString() {
        return String.format("ValueType{%s}", name);
    }
}
