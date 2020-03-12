package org.comroid.varbind.model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.varbind.bind.VarBind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class VariableCarrier {
    private final Map<VarBind<?>, AtomicReference<?>> vars;
    private final Set<VarBind<?>> initiallySet = null; //todo

    protected VariableCarrier() {
        vars = new ConcurrentHashMap<>();
    }

    public abstract Set<VarBind<?>> getBindings();

    public final Set<VarBind<?>> initiallySet() {
        return initiallySet;
    }

    public final <T> @Nullable T getVar(VarBind<T> bind) {
        return bind.cast(vars.computeIfAbsent(bind, key -> new AtomicReference<>(bind.def())).get());
    }

    public final <T> @NotNull Optional<T> wrapVar(VarBind<T> bind) {
        return Optional.ofNullable(getVar(bind));
    }
}
