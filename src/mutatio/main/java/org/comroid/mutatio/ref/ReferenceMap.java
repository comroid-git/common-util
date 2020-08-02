package org.comroid.mutatio.ref;

import org.comroid.mutatio.pipe.BiPipe;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.Pipeable;
import org.comroid.mutatio.pump.Pump;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ReferenceMap<K, V, REF extends KeyedReference<K, V>> extends Pipeable<V> {
    default REF getReference(K key) {
        return getReference(key, false);
    }

    /**
     * Gets a reference to the value at the specified key in the map.
     * As described by the {@linkplain Contract method contract}; this method will
     * <ul>
     *     <li>Fail, if the first parameter is {@code null}</li>
     *     <li>Return a {@link Nullable} Reference, if the second parameter is {@code false}</li>
     *     <li>Return q {@link NotNull} Reference, if the second parameter is {@code true}</li>
     * </ul>
     *
     * @param key The key to look at.
     * @param createIfAbsent Whether to create the reference if its non-existent
     * @return A {@link Reference}, or {@code null}
     */
    @Contract("null, _ -> fail; !null, false -> _; !null, true -> !null")
    @Nullable REF getReference(K key, boolean createIfAbsent);

    ReferenceIndex<? extends Map.Entry<K, V>> entryIndex();

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

}
