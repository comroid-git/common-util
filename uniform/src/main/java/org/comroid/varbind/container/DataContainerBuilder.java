package org.comroid.varbind.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Builder;
import org.comroid.common.ref.SelfDeclared;

import org.comroid.varbind.bind.VarBind;
import org.jetbrains.annotations.Nullable;

public abstract class DataContainerBuilder<S extends DataContainerBuilder<S, T, D>, T extends DataContainer<D>, D>
        implements Builder<T>, SelfDeclared<S> {
    private final           Map<VarBind<Object, D, ?, Object>, Object> values = new HashMap<>();
    private final           Class<T>                              type;
    private final @Nullable D                                     dependencyObject;

    public DataContainerBuilder(Class<T> type, @Nullable D dependencyObject) {
        this.type             = Objects.requireNonNull(type, "Type cannot be null");
        this.dependencyObject = dependencyObject;
    }

    public final <V> DataContainerBuilder<S, T, D> with(VarBind<V, D, ?, ?> bind, V value) {
        values.put(Polyfill.uncheckedCast(bind), value);

        return this;
    }

    @Override
    public final T build() {
        return mergeVarCarrier(new DataContainerBase<>(values, dependencyObject, type));
    }

    protected abstract T mergeVarCarrier(DataContainer<D> dataContainer);
}
