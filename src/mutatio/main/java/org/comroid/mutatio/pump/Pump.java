package org.comroid.mutatio.pump;

import org.comroid.api.ExecutorBound;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.StageAdapter;
import org.comroid.mutatio.ref.ReferenceIndex;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Pump<O, T> extends Pipe<O, T>, ExecutorBound {
    static <T> Pump<T, T> create() {
        return create(Runnable::run);
    }

    static <T> Pump<T, T> create(Executor executor) {
        return new BasicPump<>(executor, ReferenceIndex.create());
    }

    static <T> Pump<T, T> of(Collection<T> collection) {
        final Pump<T, T> pump = create();
        collection.forEach(pump);

        return pump;
    }

    @Override
    <R> Pump<T, R> addStage(StageAdapter<T, R> stage);

    <R> Pump<T, R> addStage(Executor executor, StageAdapter<T, R> stage);

    default CompletableFuture<T> next() {
        class OnceCompletingStage implements StageAdapter<T, T> {
            private final CompletableFuture<T> future = new CompletableFuture<>();

            @Override
            public synchronized T apply(T it) {
                if (!future.isDone())
                    future.complete(it);
                return null;
            }
        }

        final OnceCompletingStage stage = new OnceCompletingStage();
        addStage(stage);

        return stage.future;
    }
}
