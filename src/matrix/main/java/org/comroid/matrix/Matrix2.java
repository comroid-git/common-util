package org.comroid.matrix;

import org.comroid.api.Polyfill;
import org.comroid.trie.TrieMap;
import org.comroid.matrix.impl.MatrixCapability;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.spellbind.Spellbind;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Matrix2<X, Y, V> extends Matrix<V, Matrix2.Entry<X, Y, V>> {
    static <X, Y, V> Matrix2<X, Y, V> create() {
        return new Builder<X, Y, V>(TrieMap.ofString()).build();
    }

    default boolean containsKey(X x, Y y) {
        return containsCoordinate(generateCoordinate(x, y));
    }

    default boolean isNull(X x, Y y) {
        return isNull(generateCoordinate(x, y));
    }

    default V get(X x, Y y) {
        return getEntryAt(generateCoordinate(x, y), null).get();
    }

    default V put(X x, Y y, V value) {
        return put(generateCoordinate(x, y), value);
    }

    default V compute(X x, Y y, BiFunction<String, ? super V, ? extends V> computor) {
        return compute(generateCoordinate(x, y), computor);
    }

    default V computeIfPresent(X x, Y y, BiFunction<String, ? super V, ? extends V> computor) {
        return computeIfPresent(generateCoordinate(x, y), computor);
    }

    default V computeIfAbsent(X x, Y y, Function<String, ? extends V> supplier) {
        return computeIfAbsent(generateCoordinate(x, y), supplier);
    }

    default String generateCoordinate(X x, Y y) {
        return String.format("%s-%s", x, y);
    }

    interface Entry<X, Y, V> extends Matrix.Entry<V> {
        @Nullable
        X getX();

        @Nullable
        Y getY();
    }

    final class Builder<X, Y, V> implements org.comroid.api.Builder<Matrix2<X, Y, V>> {
        private final Spellbind.Builder<Matrix2<X, Y, V>> binder = Spellbind
                .builder(Polyfill.uncheckedCast(Matrix2.class));
        private final Map<String, Entry<X, Y, V>> initValues;

        public Spellbind.Builder<Matrix2<X, Y, V>> getBinder() {
            return binder;
        }

        public Map<String, Entry<X, Y, V>> getUnderlyingMap() {
            return initValues;
        }

        protected Builder(@Nullable Map<String, Entry<X, Y, V>> initValues) {
            this.initValues = initValues;

            final PartialMatrix<V, Entry<X, Y, V>> matrix = new PartialMatrix<>(initValues);

            binder.coreObject(new MatrixCapability.BiDimensional<>());
            binder.subImplement(matrix, Matrix.class);
            binder.subImplement(matrix, Iterable.class);
        }

        @Override
        public Matrix2<X, Y, V> build() {
            return binder.build();
        }
    }
}
