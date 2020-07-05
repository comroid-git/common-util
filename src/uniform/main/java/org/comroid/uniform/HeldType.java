package org.comroid.uniform;

import java.util.function.Function;

public interface HeldType<R> extends Function<String, R> {
    <T> T convert(R value, ValueType<T> toType);

    R apply(String from);
}
