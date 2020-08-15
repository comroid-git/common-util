package org.comroid.mutatio.ref;

import org.comroid.mutatio.pipe.BiPipe;
import org.comroid.mutatio.pipe.Pipe;
import org.comroid.mutatio.pipe.Pipeable;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.pump.Pump;
import org.jetbrains.annotations.Contract;
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

public interface ReferenceMap<K, V, REF extends KeyedReference<K, V>> extends Pipeable<V> {
    static <K, V, REF extends KeyedReference<K, V>> ReferenceMap<K, V, REF> create(
            Function<? extends Reference<V>, REF> refUnwrapper
    ) {
        return new ReferenceMap.Support.Basic<>(refUnwrapper);
    }

    default boolean put(K key, V value) {
        return getReference(key, value != null).set(value);
    }

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

    final class Support {
        public static abstract class Abstract<K, V, REF extends KeyedReference<K, V>> implements ReferenceMap<K, V, REF> {
            private final Function<? extends Reference<V>, REF> refUnwrapper;

            protected Abstract(Function<? extends Reference<V>, REF> refUnwrapper) {
                this.refUnwrapper = refUnwrapper;
            }

            @Override
            public REF getReference(K key, boolean createIfAbsent) {
                return null;
            }

            @Override
            public ReferenceIndex<Map.Entry<K, V>> entryIndex() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean containsKey(K key) {
                return false;
            }

            @Override
            public boolean containsValue(V value) {
                return false;
            }

            @Override
            public Stream<REF> stream(Predicate<K> filter) {
                return null;
            }

            @Override
            public Pipe<?, REF> pipe(Predicate<K> filter) {
                return null;
            }

            @Override
            public void forEach(BiConsumer<K, V> action) {

            }
        }

        public static class Basic<K, V, REF extends KeyedReference<K, V>> extends Abstract<K, V, REF> {
            public Basic(Function<? extends Reference<V>, REF> refUnwrapper) {
                super(refUnwrapper);
            }
        }
    }
}
