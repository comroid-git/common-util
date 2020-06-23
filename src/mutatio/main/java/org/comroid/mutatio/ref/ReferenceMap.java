package org.comroid.mutatio.ref;

import org.comroid.mutatio.pipe.BiPipe;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.Pipeable;
import org.comroid.mutatio.pump.Pump;
import org.comroid.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ReferenceMap<K, V, REF extends Reference<V>> extends Pipeable<V> {
    default REF getReference(K key) {
        return getReference(key, false);
    }

    REF getReference(K key, boolean createIfAbsent);

    ReferenceIndex<Map.Entry<K, V>> entryIndex();

    default V get(K key) {
        return getReference(key).get();
    }

    default Optional<V> wrap(K key) {
        return getReference(key).wrap();
    }

    default @NotNull V requireNonNull(K key) {
        return getReference(key).requireNonNull();
    }

    default @NotNull V requireNonNull(K key, String message) {
        return getReference(key).requireNonNull(message);
    }

    @Override
    default Pipe<?, V> pipe() {
        return entryIndex()
                .pipe()
                .map(Map.Entry::getValue);
    }

    default BiPipe<?, ?, K, V> biPipe() {
        return entryIndex()
                .pipe()
                .bi(Map.Entry::getValue)
                .mapFirst(Map.Entry::getKey);
    }

    @Override
    default Pump<?, V> pump(Executor executor) {
        return pipe().pump(executor);
    }

    interface Settable<K, V, REF extends Reference.Settable<V>> extends ReferenceMap<K, V, REF> {
        default @Nullable V set(K key, V newValue) {
            return getReference(key).set(newValue);
        }

        default @Nullable V compute(K key, Function<V, V> computor) {
            return getReference(key, true).compute(computor);
        }

        default @Nullable V computeIfPresent(K key, Function<V, V> computor) {
            return getReference(key, true).computeIfPresent(computor);
        }

        default @Nullable V computeIfAbsent(K key, Supplier<V> supplier) {
            return getReference(key, true).computeIfAbsent(supplier);
        }
    }
}
