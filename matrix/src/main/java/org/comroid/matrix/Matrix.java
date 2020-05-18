package org.comroid.matrix;

import org.comroid.common.info.Valued;
import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.Named;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Matrix<V, E extends Matrix.Entry<V>> extends Iterable<E> {
    @Nullable
    E getEntryAt(String coordinate, boolean createIfAbsent);

    boolean containsCoordinate(String coordinate);

    @Nullable
    E compute(String coordinate, BiFunction<String, ? super E, ? extends E> computor);

    @Nullable
    E computeIfPresent(String coordinate, BiFunction<String, ? super E, ? extends E> computor);

    @Nullable
    E computeIfAbsent(String coordinate, Function<? super String, ? extends E> supplier);

    interface Entry<V> extends Reference.Settable<V>, Named, Valued<V> {
        String getCoordinate();

        @Override
        default String getName() {
            return getCoordinate();
        }

        @Override
        @Nullable
        default V getValue() {
            return get();
        }
    }
}
