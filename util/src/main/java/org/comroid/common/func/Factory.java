package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;

public interface Factory<T> extends Provider.Now<T> {
    int counter();

    T create();

    @Override
    default T now() {
        return create();
    }
}
