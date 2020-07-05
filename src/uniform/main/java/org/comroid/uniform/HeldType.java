package org.comroid.uniform;

import org.comroid.common.ref.Named;

import java.util.function.Function;

public interface HeldType<R> extends Function<String, R>, Named {
    <T> T convert(R value, ValueType<T> toType);

    R apply(String from);
}
