package org.comroid.matrix.impl;

import org.comroid.matrix.Matrix;
import org.comroid.spellbind.model.TypeFragment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PartialMatrix<V, E extends Matrix.Entry<V>> extends AbstractMatrix<V, E> {
    public PartialMatrix(Map<String, E> underlying) {
        super(underlying);
    }

    @NotNull
    @Override
    protected E createEntry(String key, @Nullable V initialValue) {
        throw new UnsupportedOperationException("Cannot create entries");
    }

    public interface TwoDim<X, Y, V, E extends Matrix.Entry<V>>
            extends TypeFragment<Matrix<V, E>> {
        boolean containsKey(X x, Y y);

        boolean isNull(X x, Y y);

        V get(X x, Y y);

        V put(X x, Y y, V value);

        V compute(X x, Y y, BiFunction<String, ? super V, ? extends V> computor);

        V computeIfPresent(X x, Y y, BiFunction<String, ? super V, ? extends V> computor);

        V computeIfAbsent(X x, Y y, Function<String, ? extends V> supplier);

        String generateCoordinate(X x, Y y);
    }

    public interface ThreeDim<X, Y, Z, V, E extends Matrix.Entry<V>>
            extends TypeFragment<Matrix<V, E>> {
        boolean containsKey(X x, Y y, Z z);

        boolean isNull(X x, Y y, Z z);

        V get(X x, Y y, Z z);

        V put(X x, Y y, Z z, V value);

        V compute(X x, Y y, Z z, BiFunction<String, ? super V, ? extends V> computor);

        V computeIfPresent(X x, Y y, Z z, BiFunction<String, ? super V, ? extends V> computor);

        V computeIfAbsent(X x, Y y, Z z, Function<? super String, ? extends V> supplier);

        String generateCoordinate(X x, Y y, Z z);
    }
}
