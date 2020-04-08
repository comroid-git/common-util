package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;

public interface Creator<T> extends Provider<T> {
    CompletableFuture<T> create();

    @Override
    default CompletableFuture<T> get() {
        return create();
    }
}
