package org.comroid.matrix;

import org.comroid.common.Polyfill;
import org.comroid.common.map.TrieMap;
import org.comroid.matrix.impl.MatrixCapability;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.spellbind.Spellbind;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Matrix3<X, Y, Z, V> extends Matrix<V, Matrix3.Entry<X, Y, Z, V>> {
    static <X, Y, Z, V> Matrix3<X, Y, Z, V> create() {
        return new Builder<X, Y, Z, V>(TrieMap.ofString()).build();
    }

    default boolean containsKey(X x, Y y, Z z) {
        return containsCoordinate(generateCoordinate(x, y, z));
    }

    default boolean isNull(X x, Y y, Z z) {
        return isNull(generateCoordinate(x, y, z));
    }

    default V get(X x, Y y, Z z) {
        return get(generateCoordinate(x, y, z));
    }

    default V put(X x, Y y, Z z, V value) {
        return put(generateCoordinate(x, y, z), value);
    }

    default V compute(X x, Y y, Z z, BiFunction<String, ? super V, ? extends V> computor) {
        return compute(generateCoordinate(x, y, z), computor);
    }

    default V computeIfPresent(X x, Y y, Z z, BiFunction<String, ? super V, ? extends V> computor) {
        return computeIfPresent(generateCoordinate(x, y, z), computor);
    }

    default V computeIfAbsent(X x, Y y, Z z, Function<? super String, ? extends V> supplier) {
        return computeIfAbsent(generateCoordinate(x, y, z), supplier);
    }

    default String generateCoordinate(X x, Y y, Z z) {
        return String.format("%s-%s-%s", x, y, z);
    }

    interface Entry<X, Y, Z, V> extends Matrix.Entry<V> {
        X getX();

        Y getY();

        Z getZ();
    }

    final class Builder<X, Y, Z, V> implements org.comroid.common.func.Builder<Matrix3<X, Y, Z, V>> {
        private final Spellbind.Builder<Matrix3<X, Y, Z, V>> binder = Spellbind
                .builder(Polyfill.uncheckedCast(Matrix3.class));
        private final Map<String, Entry<X, Y, Z, V>> initValues;

        public Spellbind.Builder<Matrix3<X, Y, Z, V>> getBinder() {
            return binder;
        }

        public Map<String, Entry<X, Y, Z, V>> getUnderlyingMap() {
            return initValues;
        }

        protected Builder(@Nullable Map<String, Entry<X, Y, Z, V>> initValues) {
            this.initValues = initValues;

            binder.coreObject(new MatrixCapability.TriDimensional<>());
            binder.subImplement(new PartialMatrix<>(initValues), Matrix.class);
        }

        @Override
        public Matrix3<X, Y, Z, V> build() {
            return binder.build();
        }
    }
}
