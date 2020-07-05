package org.comroid.uniform;

import org.comroid.api.Junction;
import org.comroid.common.ref.Named;

import java.util.function.Function;

public interface HeldType<R> extends Named {
    Junction<String, R> getConverter();

    <T> T convert(R value, ValueType<T> toType);
}
