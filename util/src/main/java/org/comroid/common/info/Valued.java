package org.comroid.common.info;

import org.jetbrains.annotations.Nullable;

public interface Valued<V> {
    @Nullable
    V getValue();
}
