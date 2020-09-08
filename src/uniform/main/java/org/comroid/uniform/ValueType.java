package org.comroid.uniform;

import org.comroid.api.Junction;

import java.util.function.Function;
import java.util.function.Predicate;

public final class ValueType<R> implements HeldType<R>, Predicate<Object> {
    public static final ValueType<Boolean> BOOLEAN
            = new ValueType<>(Boolean.class, "boolean", Boolean::parseBoolean);
    public static final ValueType<Character> CHARACTER
            = new ValueType<>(Character.class, "char", str -> str.toCharArray()[0]);
    public static final ValueType<Double> DOUBLE
            = new ValueType<>(Double.class, "double", Double::parseDouble);
    public static final ValueType<Float> FLOAT
            = new ValueType<>(Float.class, "float", Float::parseFloat);
    public static final ValueType<Integer> INTEGER
            = new ValueType<>(Integer.class, "int", Integer::parseInt);
    public static final ValueType<Long> LONG
            = new ValueType<>(Long.class, "long", Long::parseLong);
    public static final ValueType<Short> SHORT
            = new ValueType<>(Short.class, "short", Short::parseShort);
    public static final ValueType<String> STRING
            = new ValueType<>(String.class, "String", Function.identity());

    public static final ValueType<Void> VOID
            = new ValueType<>(Void.class, "Void", it -> null);

    private final Class<R> type;
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

    public ValueType(Class<R> type, String name, Function<String, R> mapper) {
        this.type = type;
        this.name = name;
        this.converter = Junction.ofString(mapper);
    }

    public static <T> ValueType<T> typeOf(T value) {
        return null;
    }

    @Override
    public <T> T convert(R value, HeldType<T> toType) {
        return toType.getConverter().forward(value.toString());
    }

    @Override
    public String toString() {
        return String.format("ValueType{%s}", name);
    }

    @Override
    public boolean test(Object it) {
        return type.isInstance(it);
    }
}
