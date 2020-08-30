package org.comroid.matrix.impl;

import org.comroid.matrix.Matrix;
import org.comroid.matrix.Matrix2;
import org.comroid.matrix.Matrix3;
import org.comroid.mutatio.ref.FutureReference;
import org.comroid.mutatio.ref.Reference;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Formattable;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class MatrixCapability {
    private static String wrapFormattable(Object it) {
        if (it instanceof Formattable)
            return String.format("%#s", it);
        return it.toString();
    }

    @Internal
    private static abstract class AbstractEntry<V> extends Reference.Support.Base<V> implements Matrix.Entry<V> {
        protected final String coordinate;
        protected final Reference<V> valueRef;

        @Override
        public final String getCoordinate() {
            return coordinate;
        }

        protected AbstractEntry(String coordinate, @Nullable V initialValue) {
            super(true);

            this.coordinate = coordinate;
            this.valueRef = Reference.create(initialValue);
        }

        @Nullable
        @Override
        protected final V doGet() {
            return valueRef.get();
        }

        @Override
        protected final boolean doSet(V newValue) {
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
    public static final class BiDimensional<X, Y, V>
            extends AbstractMatrix<V, Matrix2.Entry<X, Y, V>>
            implements Matrix2<X, Y, V> {
        @Internal
        public BiDimensional() {
            super(null);
        }

        @NotNull
        @Override
        protected Matrix2.Entry<X, Y, V> createEntry(String key, @Nullable V initialValue) {
            return new BiDimensionalEntry<>(key, initialValue);
        }

        @Override
        public boolean containsKey(X x, Y y) {
            return containsCoordinate(generateCoordinate(x, y));
        }

        @Override
        public boolean isNull(X x, Y y) {
            return isNull(generateCoordinate(x, y));
        }

        @Override
        public V get(X x, Y y) {
            return getEntryAt(generateCoordinate(x, y), null).get();
        }

        @Override
        public boolean put(X x, Y y, V value) {
            return put(generateCoordinate(x, y), value);
        }

        @Override
        public V compute(X x, Y y, BiFunction<String, ? super V, ? extends V> computor) {
            return compute(generateCoordinate(x, y), computor);
        }

        @Override
        public V computeIfPresent(X x, Y y, BiFunction<String, ? super V, ? extends V> computor) {
            return computeIfPresent(generateCoordinate(x, y), computor);
        }

        @Override
        public V computeIfAbsent(X x, Y y, Function<String, ? extends V> supplier) {
            return computeIfAbsent(generateCoordinate(x, y), supplier);
        }

        @Override
        public V remove(X x, Y y) {
            return remove(generateCoordinate(x, y));
        }

        @Override
        public String generateCoordinate(X x, Y y) {
            return String.format("%s-%s", wrapFormattable(x), wrapFormattable(y));
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
    public static final class TriDimensional<X, Y, Z, V>
            extends AbstractMatrix<V, Matrix3.Entry<X, Y, Z, V>>
            implements Matrix3<X, Y, Z, V> {
        @Internal
        public TriDimensional() {
            super(null);
        }

        @NotNull
        @Override
        protected Matrix3.Entry<X, Y, Z, V> createEntry(String key, @Nullable V initialValue) {
            return new TriDimensionalEntry<>(key, initialValue);
        }

        @Override
        public boolean containsKey(X x, Y y, Z z) {
            return containsCoordinate(generateCoordinate(x, y, z));
        }

        @Override
        public boolean isNull(X x, Y y, Z z) {
            return isNull(generateCoordinate(x, y, z));
        }

        @Override
        public V get(X x, Y y, Z z) {
            return get(generateCoordinate(x, y, z));
        }

        @Override
        public boolean put(X x, Y y, Z z, V value) {
            return put(generateCoordinate(x, y, z), value);
        }

        @Override
        public V compute(X x, Y y, Z z, BiFunction<String, ? super V, ? extends V> computor) {
            return compute(generateCoordinate(x, y, z), computor);
        }

        @Override
        public V computeIfPresent(X x, Y y, Z z, BiFunction<String, ? super V, ? extends V> computor) {
            return computeIfPresent(generateCoordinate(x, y, z), computor);
        }

        @Override
        public V computeIfAbsent(X x, Y y, Z z, Function<? super String, ? extends V> supplier) {
            return computeIfAbsent(generateCoordinate(x, y, z), supplier);
        }

        @Override
        public V remove(X x, Y y, Z z) {
            return remove(generateCoordinate(x, y, z));
        }

        @Override
        public String generateCoordinate(X x, Y y, Z z) {
            return String.format("%s-%s-%s", wrapFormattable(x), wrapFormattable(y), wrapFormattable(z));
        }
    }
}
