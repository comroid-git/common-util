package org.comroid.matrix.impl;

import org.comroid.matrix.Matrix;
import org.comroid.matrix.Matrix2;
import org.comroid.matrix.Matrix3;
import org.comroid.mutatio.ref.FutureReference;
import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MatrixCapability {
    @Internal
    private static abstract class AbstractEntry<V> implements Matrix.Entry<V> {
        protected final String coordinate;
        protected final Reference.Settable<V> valueRef;

        @Override
        public final String getCoordinate() {
            return coordinate;
        }

        protected AbstractEntry(String coordinate, @Nullable V initialValue) {
            this.coordinate = coordinate;
            this.valueRef = Reference.Settable.create(initialValue);
        }

        @Nullable
        @Override
        public final V get() {
            return valueRef.get();
        }

        @Nullable
        @Override
        public final V set(V newValue) {
            return valueRef.set(newValue);
        }
    }

    public static final class BiDimensionalEntry<X, Y, V>
            extends AbstractEntry<V>
            implements Matrix2.Entry<X, Y, V> {
        public final FutureReference<X> xRef = new FutureReference<>();
        public final FutureReference<Y> yRef = new FutureReference<>();

        @Override
        public X getX() {
            return xRef.get();
        }

        @Override
        public Y getY() {
            return yRef.get();
        }

        protected BiDimensionalEntry(String coordinate, @Nullable V initialValue) {
            super(coordinate, initialValue);
        }
    }

    @Internal
    public static final class BiDimensional<X, Y, V> extends AbstractMatrix<V, Matrix2.Entry<X, Y, V>> {
        @Internal
        public BiDimensional() {
            super(null);
        }

        @NotNull
        @Override
        protected Matrix2.Entry<X, Y, V> createEntry(String key, @Nullable V initialValue) {
            return new BiDimensionalEntry<>(key, initialValue);
        }
    }

    public static final class TriDimensionalEntry<X, Y, Z, V>
            extends AbstractEntry<V>
            implements Matrix3.Entry<X, Y, Z, V> {
        public final FutureReference<X> xRef = new FutureReference<>();
        public final FutureReference<Y> yRef = new FutureReference<>();
        public final FutureReference<Z> zRef = new FutureReference<>();

        @Override
        public X getX() {
            return xRef.get();
        }

        @Override
        public Y getY() {
            return yRef.get();
        }

        @Override
        public Z getZ() {
            return zRef.get();
        }

        public TriDimensionalEntry(String key, @Nullable V initialValue) {
            super(key, initialValue);
        }
    }

    @Internal
    public static final class TriDimensional<X, Y, Z, V> extends AbstractMatrix<V, Matrix3.Entry<X, Y, Z, V>> {
        @Internal
        public TriDimensional() {
            super(null);
        }

        @NotNull
        @Override
        protected Matrix3.Entry<X, Y, Z, V> createEntry(String key, @Nullable V initialValue) {
            return new TriDimensionalEntry<>(key, initialValue);
        }
    }
}
