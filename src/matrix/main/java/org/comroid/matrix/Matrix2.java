package org.comroid.matrix;

import org.comroid.api.Polyfill;
import org.comroid.matrix.impl.MatrixCapability;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.spellbind.SpellCore;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Matrix2<X, Y, V> extends Matrix<V, Matrix2.Entry<X, Y, V>>, PartialMatrix.TwoDim<X, Y, V> {
    static <X, Y, V> Matrix2<X, Y, V> create() {
        return new Builder<X, Y, V>(TrieMap.ofString()).build();
    }

    @Override
    default boolean isNull(String coordinate) {
        return getEntryAt(coordinate, null).isNull();
    }

    interface Entry<X, Y, V> extends Matrix.Entry<V> {
        X getX();

        Y getY();
    }

    final class Builder<X, Y, V> implements org.comroid.api.Builder<Matrix2<X, Y, V>> {
        private final Map<String, Entry<X, Y, V>> initValues;
        private final SpellCore.Builder<Matrix2<X, Y, V>> binder = SpellCore.builder(
                Polyfill.uncheckedCast(Matrix2.class),
                new MatrixCapability.BiDimensional<>()
        );

        public SpellCore.Builder<Matrix2<X, Y, V>> getBinder() {
            return binder;
        }

        public Map<String, Entry<X, Y, V>> getUnderlyingMap() {
            return initValues;
        }

        protected Builder(@Nullable Map<String, Entry<X, Y, V>> initValues) {
            this.initValues = initValues;
            binder.addFragment(Matrix.fragmentProvider());
        }

        @Override
        public Matrix2<X, Y, V> build() {
            return binder.build(initValues);
        }
    }
}
