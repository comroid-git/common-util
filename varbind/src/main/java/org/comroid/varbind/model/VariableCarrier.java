package org.comroid.varbind.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.common.iter.Span;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.varbind.bind.VarBind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.comroid.common.Polyfill.deadCast;

public abstract class VariableCarrier<BAS, OBJ extends BAS, DEP> {
    private final SeriLib<BAS, OBJ, ? extends BAS> seriLib;
    private final Map<VarBind<?, ?, DEP, ?, OBJ>, AtomicReference<Span<Object>>> vars = new ConcurrentHashMap<>();
    private final Set<VarBind<?, ?, DEP, ?, OBJ>> binds;
    private final Set<VarBind<?, ?, DEP, ?, OBJ>> initiallySet;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<DEP> dependencyObject;

    protected <ARR extends BAS> VariableCarrier(
            SeriLib<BAS, OBJ, ARR> seriLib,
            @Nullable String data,
            @Nullable DEP dependencyObject
    ) {
        this(seriLib, data == null ? null : seriLib.objectType.cast(seriLib.parser.forward(data)), dependencyObject);
    }

    protected <ARR extends BAS> VariableCarrier(
            SeriLib<BAS, OBJ, ARR> seriLib,
            @Nullable OBJ node,
            @Nullable DEP dependencyObject
    ) {
        this.seriLib = seriLib;
        this.binds = findBinds(getClass());
        this.initiallySet = initializeVariables(seriLib.dummy(node));
        this.dependencyObject = Optional.ofNullable(dependencyObject);
    }

    private Set<VarBind<?, ?, DEP, ?, OBJ>> findBinds(Class<? extends VariableCarrier> inClass) {
        final VarBind.Location location = inClass.getAnnotation(VarBind.Location.class);

        if (location == null)
            throw new IllegalStateException(String.format("Class %s extends VariableCarrier, but does not have a %s annotation.",
                    inClass.getName(), VarBind.Location.class.getName()));

        return ReflectionHelper.collectStaticFields(VarBind.class, location.value());
    }

    private <SERI extends SeriLib<BAS, OBJ, ARR>, ARR extends BAS, TAR extends BAS> Set<VarBind<?, ?, DEP, ?, OBJ>> initializeVariables(
            @Nullable NodeDummy<SERI, BAS, OBJ, ARR, TAR> initalData) {
        if (initalData == null) return emptySet();

        final HashSet<VarBind<?, ?, DEP, ?, OBJ>> initialized = new HashSet<>();
        for (VarBind<?, ?, DEP, ?, OBJ> bind : this.binds) {
            if (initalData.containsKey(bind.getName())) {
                ref((VarBind<Object, Object, DEP, Object, OBJ>) bind)
                        .set((Span<Object>) bind.extract(initalData.obj()));

                initialized.add(bind);
            }
        }

        return unmodifiableSet(initialized);
    }

    private <C> AtomicReference<Span<C>> ref(VarBind<C, ?, DEP, ?, OBJ> bind) {
        return deadCast(vars.computeIfAbsent(bind, key ->
                deadCast(new AtomicReference<>(new Span<C>(1, Span.NullPolicy.IGNORE, false)))));
    }

    public final SeriLib<BAS, OBJ, ? extends BAS> getSerializationLibrary() {
        return seriLib;
    }

    public final Set<VarBind<?, ?, DEP, ?, OBJ>> getBindings() {
        return binds;
    }

    public final Set<VarBind<?, ?, DEP, ?, OBJ>> initiallySet() {
        return initiallySet;
    }

    public final <T, A, R> @Nullable R getVar(VarBind<T, A, DEP, R, OBJ> bind) {
        return bind.finish(ref(bind).get()
                .stream()
                .map(it -> bind.remap(it, dependencyObject.orElse(null)))
                .collect(Span.collector()));
    }

    public final <T, A, R> @NotNull Optional<R> wrapVar(VarBind<T, A, DEP, R, OBJ> bind) {
        return Optional.ofNullable(getVar(bind));
    }
}
