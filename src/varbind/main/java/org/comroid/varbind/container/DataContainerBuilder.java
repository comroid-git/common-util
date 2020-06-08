package org.comroid.varbind.container;

import org.comroid.api.Polyfill;
import org.comroid.api.Builder;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.varbind.bind.VarBind;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class DataContainerBuilder<S extends DataContainerBuilder<S, T, D>, T extends DataContainer<D>, D>
        implements Builder<T>, SelfDeclared<S> {
    private final Map<VarBind<Object, D, ?, Object>, Object> values = new HashMap<>();
    private final Class<T> type;
    private final @Nullable D dependencyObject;

    public DataContainerBuilder(Class<T> type, @Nullable D dependencyObject) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.dependencyObject = dependencyObject;
    }

    public final <V> S with(VarBind<V, ? super D, ?, ?> bind, V value) {
        values.put(Polyfill.uncheckedCast(bind), value);

        return Polyfill.uncheckedCast(this);
    }

    @Override
    public final T build() {
        return mergeVarCarrier(new DataContainerBase<>(values, dependencyObject, type));
    }

    protected abstract T mergeVarCarrier(DataContainer<D> dataContainer);
}
