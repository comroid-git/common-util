package org.comroid.matrix;

import org.comroid.api.Polyfill;
import org.comroid.matrix.impl.MatrixCapability;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.spellbind.SpellCore;
import org.comroid.trie.TrieMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Matrix3<X, Y, Z, V> extends Matrix<V, Matrix3.Entry<X, Y, Z, V>>, PartialMatrix.ThreeDim<X, Y, Z, V> {
    static <X, Y, Z, V> Matrix3<X, Y, Z, V> create() {
        return new Builder<X, Y, Z, V>(TrieMap.ofString()).build();
    }

    interface Entry<X, Y, Z, V> extends Matrix.Entry<V> {
        X getX();

        Y getY();

        Z getZ();
    }

    final class Builder<X, Y, Z, V> implements org.comroid.api.Builder<Matrix3<X, Y, Z, V>> {
        private final SpellCore.Builder<Matrix3<X, Y, Z, V>> binder = SpellCore.builder(
                Polyfill.uncheckedCast(Matrix3.class),
                new MatrixCapability.TriDimensional<>()
        );
        private final Map<String, Entry<X, Y, Z, V>> initValues;

        public SpellCore.Builder<Matrix3<X, Y, Z, V>> getBinder() {
            return binder;
        }

        public Map<String, Entry<X, Y, Z, V>> getUnderlyingMap() {
            return initValues;
        }

        protected Builder(@Nullable Map<String, Entry<X, Y, Z, V>> initValues) {
            this.initValues = initValues;
            binder.addFragment(Matrix.fragmentProvider());
        }

        @Override
        public Matrix3<X, Y, Z, V> build() {
            return binder.build(initValues);
        }
    }
}
