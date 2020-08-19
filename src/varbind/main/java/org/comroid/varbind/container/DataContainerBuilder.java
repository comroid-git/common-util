package org.comroid.varbind.container;

import org.comroid.api.Builder;
import org.comroid.api.Polyfill;
import org.comroid.api.SelfDeclared;
import org.comroid.varbind.bind.VarBind;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class DataContainerBuilder<S extends DataContainerBuilder<S, T>, T extends DataContainer<? super T>>
        implements Builder<T>, SelfDeclared<S> {
    private final Map<VarBind<? super T, Object, ?, Object>, Object> values = new HashMap<>();
    private final Class<T> type;

    public DataContainerBuilder(Class<T> type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
    }

    public final <V> S with(VarBind<? super T, V, ?, ?> bind, V value) {
        values.put(Polyfill.uncheckedCast(bind), value);

        return Polyfill.uncheckedCast(this);
    }

    @Override
    public final T build() {
        //noinspection unchecked
        return mergeVarCarrier(new DataContainerBase<T>((Map) values, type));
    }

    protected abstract T mergeVarCarrier(DataContainer<? super T> dataContainer);
}
