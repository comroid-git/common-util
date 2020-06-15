package org.comroid.matrix;

import org.comroid.api.Invocable;
import org.comroid.api.Polyfill;
import org.comroid.common.info.Valued;
import org.comroid.common.ref.Named;
import org.comroid.matrix.impl.PartialMatrix;
import org.comroid.mutatio.ref.Reference;
import org.comroid.spellbind.model.TypeFragment;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Matrix<V, E extends Matrix.Entry<V>> extends Iterable<E>, TypeFragment<Matrix<V, E>> {
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

    V put(String coordinate, V value);

    @Nullable
    V compute(String coordinate, BiFunction<String, ? super V, ? extends V> computor);

    @Nullable
    V computeIfPresent(String coordinate, BiFunction<String, ? super V, ? extends V> computor);

    @Nullable
    V computeIfAbsent(String coordinate, Function<? super String, ? extends V> supplier);

    default boolean isNull(String coordinate) {
        return getEntryAt(coordinate, null).isNull();
    }

    @NotNull
    E getEntryAt(String coordinate, @Nullable V initialValue);

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
