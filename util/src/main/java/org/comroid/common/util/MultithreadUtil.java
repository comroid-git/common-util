package org.comroid.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public final class MultithreadUtil {
    public static <T> CompletableFuture<T> submitQuickTask(ExecutorService executorService, Callable<T> task) {
        final CompletableFuture<T> future = new CompletableFuture<>();

        executorService.submit(() -> {
            T result = null;

            try {
                result = task.call();
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                future.complete(result);
            }
        });

        return future;
    }
}
