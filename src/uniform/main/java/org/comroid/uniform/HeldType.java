package org.comroid.uniform;

import org.comroid.api.Junction;
import org.comroid.common.ref.Named;

public interface HeldType<R> extends Named {
    Junction<String, R> getConverter();

    <T> T convert(R value, ValueType<T> toType);
}
