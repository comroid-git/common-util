package org.comroid.varbind.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.varbind.bind.VarBind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.comroid.common.Polyfill.deadCast;

public abstract class VariableCarrier<BAS> {
    private final Map<VarBind<?, BAS, ? extends BAS>, AtomicReference<Object>> vars;
    private final Set<VarBind<?, BAS, ? extends BAS>> binds;
    private final Set<VarBind<?, BAS, ? extends BAS>> initiallySet;

    protected <SERI extends SeriLib<BAS, OBJ, ARR>, OBJ extends BAS, ARR extends BAS, TAR extends BAS> VariableCarrier(
            Set<VarBind<?, BAS, ? extends BAS>> allBinds,
            @Nullable NodeDummy<SERI, BAS, OBJ, ARR, TAR> initalData
    ) {
        this.binds = Collections.unmodifiableSet(allBinds);
        this.vars = new ConcurrentHashMap<>();

        if (initalData == null) {
            this.initiallySet = Collections.unmodifiableSet(new HashSet<>(0));
            return;
        }

        final HashSet<VarBind<?, BAS, ? extends BAS>> initialized = new HashSet<>();
        for (VarBind<?, BAS, ? extends BAS> bind : this.binds) {
            if (initalData.containsKey(bind.name())) {
                final Object value = initalData.extract(deadCast(bind.converter()));

                vars.compute(bind, (k, v) -> new AtomicReference<>(value));

                initialized.add(bind);
            }
        }
        this.initiallySet = Collections.unmodifiableSet(initialized);
    }

    public final Set<VarBind<?, BAS, ? extends BAS>> getBindings() {
        return binds;
    }

    public final Set<VarBind<?, BAS, ? extends BAS>> initiallySet() {
        return initiallySet;
    }

    public final <T> @Nullable T getVar(VarBind<T, BAS, ? extends BAS> bind) {
        return bind.cast(vars.computeIfAbsent(bind, key -> new AtomicReference<>(bind.def())).get());
    }

    public final <T> @NotNull Optional<T> wrapVar(VarBind<T, BAS, ? extends BAS> bind) {
        return Optional.ofNullable(getVar(bind));
    }
}
