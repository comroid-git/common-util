package org.comroid.mutatio.ref;

import org.comroid.mutatio.pipe.BiPipe;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.Pipeable;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.pump.Pump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    int size();

    boolean containsKey(K key);

    boolean containsValue(V value);

    default Stream<? extends V> stream() {
        return stream(any -> true).map(Reference::get);
    }

    Stream<REF> stream(Predicate<K> filter);

    @Override
    default Pipe<?, ? extends V> pipe() {
        return entryIndex()
                .pipe()
                .map(Map.Entry::getValue);
    }

    Pipe<?, REF> pipe(Predicate<K> filter);

    @Override
    default Pump<?, ? extends V> pump(Executor executor) {
        return pipe().pump(executor);
    }

    default BiPipe<?, ?, ? extends K, ? extends V> biPipe() {
        return entryIndex()
                .pipe()
                .bi(Map.Entry::getValue)
                .mapFirst(Map.Entry::getKey);
    }

    default <R> Processor<R> accessor(String key, Processor.Advancer<? super V, ? extends R> advancer);

    /**
     * @return The new value if it could be set, else the previous value.
     */
    default boolean set(K key, V newValue) {
        return getReference(key).set(newValue);
    }

    /**
     * @return The new value if it could be set, else the previous value.
     */
    default @Nullable V compute(K key, Function<V, V> computor) {
        return getReference(key, true).compute(computor);
    }

    /**
     * @return The new value if it could be set, else the previous value.
     */
    default @Nullable V computeIfPresent(K key, Function<V, V> computor) {
        return getReference(key, true).computeIfPresent(computor);
    }

    /**
     * @return The new value if it could be set, else the previous value.
     */
    default @Nullable V computeIfAbsent(K key, Supplier<V> supplier) {
        return getReference(key, true).computeIfAbsent(supplier);
    }

    void forEach(BiConsumer<K, V> action);
}
