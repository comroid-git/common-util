package org.comroid.matrix;

import org.comroid.common.Polyfill;
import org.comroid.common.map.TrieMap;
import org.comroid.matrix.impl.MatrixCapability;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.spellbind.Spellbind;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Matrix2<X, Y, V> extends Matrix<V, Matrix2.Entry<X, Y, V>> {
    static <X, Y, V> Matrix2<X, Y, V> create() {
        return new Builder<X, Y, V>(TrieMap.ofString()).build();
    }

    default String generateCoordinate(X x, Y y) {
        return String.format("%s-%s", x, y);
    }

    interface Entry<X, Y, V> extends Matrix.Entry<V> {
        X getX();

        Y getY();
    }

    final class Builder<X, Y, V> implements org.comroid.common.func.Builder<Matrix2<X, Y, V>> {
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

            binder.coreObject(new PartialMatrix<>(initValues));
            binder.subImplement(new MatrixCapability.BiDimensional<>(), Matrix2.class);
        }

        @Override
        public Matrix2<X, Y, V> build() {
            return binder.build();
        }
    }
}
