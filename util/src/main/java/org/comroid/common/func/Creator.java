package org.comroid.common.func;

import java.util.concurrent.CompletableFuture;

public interface Creator<T> {
    CompletableFuture<T> create();
}
