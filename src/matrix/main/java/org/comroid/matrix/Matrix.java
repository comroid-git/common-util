package org.comroid.matrix;

import org.comroid.api.Invocable;
import org.comroid.api.Polyfill;
import org.comroid.api.Named;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.mutatio.ref.KeyedReference;
import org.comroid.mutatio.ref.ReferenceMap;
import org.comroid.spellbind.model.TypeFragment;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Matrix<V, E extends Matrix.Entry<V>> extends Iterable<E>, TypeFragment<Matrix<V, E>>, ReferenceMap<String, V> {
    static <V, E extends Matrix.Entry<V>> TypeFragmentProvider<Matrix<V, E>> fragmentProvider() {
        return new TypeFragmentProvider<Matrix<V, E>>() {
            @Override
            public Class<Matrix<V, E>> getInterface() {
                return Polyfill.uncheckedCast(Matrix.class);
            }

            @Override
            public Invocable<? extends Matrix<V, E>> getInstanceSupplier() {
                return Invocable.ofConstructor(Polyfill.<Class<Matrix<V, E>>>uncheckedCast(PartialMatrix.class));
            }
        };
    }

    boolean containsCoordinate(String coordinate);

    @Nullable
    V get(String coordinate);

    boolean put(String coordinate, V value);

    @Nullable
    V compute(String coordinate, BiFunction<String, ? super V, ? extends V> computor);

    @Nullable
    V computeIfPresent(String coordinate, BiFunction<String, ? super V, ? extends V> computor);

    @Nullable
    V computeIfAbsent(String coordinate, Function<? super String, ? extends V> supplier);

    @Nullable
    V remove(String coordinate);

    boolean isNull(String coordinate);

    @NotNull
    E getEntryAt(String coordinate, @Nullable V initialValue);

    interface Entry<V> extends KeyedReference<String, V>, Named {
        String getCoordinate();

        @Override
        default String getName() {
            return String.format("%s -> %s", getCoordinate(), getValue());
        }

        @Override
        default String getKey() {
            return getCoordinate();
        }
    }
}
