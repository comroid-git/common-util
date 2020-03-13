package org.comroid.varbind.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.common.Polyfill;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.NodeDummy;
import org.comroid.uniform.data.SeriLib;
import org.comroid.varbind.bind.VarBind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public abstract class VariableCarrier<BAS, OBJ extends BAS, DEP> {
    private final SeriLib<BAS, OBJ, ? extends BAS> seriLib;
    private final Map<VarBind<?, ?, BAS, OBJ>, AtomicReference<Object>> vars = new ConcurrentHashMap<>();
    private final Set<VarBind<?, ?, BAS, OBJ>> binds;
    private final Set<VarBind<?, ?, BAS, OBJ>> initiallySet;
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

    private Set<VarBind<?, ?, BAS, OBJ>> findBinds(Class<? extends VariableCarrier> inClass) {
        final VarBind.Location location = inClass.getAnnotation(VarBind.Location.class);

        if (location == null)
            throw new IllegalStateException(String.format("Class %s extends VariableCarrier, but does not have a %s annotation.",
                    inClass.getName(), VarBind.Location.class.getName()));

        return ReflectionHelper.collectStaticFields(Polyfill.deadCast(VarBind.class), location.value());
    }

    private <SERI extends SeriLib<BAS, OBJ, ARR>, ARR extends BAS, TAR extends BAS> Set<VarBind<?, ?, BAS, OBJ>> initializeVariables(
            @Nullable NodeDummy<SERI, BAS, OBJ, ARR, TAR> initalData) {
        if (initalData == null) return emptySet();

        final HashSet<VarBind<?, ?, BAS, OBJ>> initialized = new HashSet<>();
        for (VarBind<?, ?, BAS, OBJ> bind : this.binds) {
            if (initalData.containsKey(bind.name())) {
                ref((VarBind<Object, Object, BAS, OBJ>) bind)
                        .set(bind.extract(initalData.obj()));

                initialized.add(bind);
            }
        }

        return unmodifiableSet(initialized);
    }

    private <T, C> AtomicReference<C> ref(VarBind<T, C, BAS, OBJ> bind) {
        return (AtomicReference<C>) vars.computeIfAbsent(bind, key -> new AtomicReference<>(null));
    }

    public final SeriLib<BAS, OBJ, ? extends BAS> getSerializationLibrary() {
        return seriLib;
    }

    public final Set<VarBind<?, ?, BAS, OBJ>> getBindings() {
        return binds;
    }

    public final Set<VarBind<?, ?, BAS, OBJ>> initiallySet() {
        return initiallySet;
    }

    public final <T, C> @Nullable T getVar(VarBind<T, C, ?, ?> bind) {
        final C ref = ref((VarBind<T, C, BAS, OBJ>) bind).get();

        if (dependencyObject.isPresent() && bind instanceof VarBind.Dep)
            return (T) ((VarBind.Dep) bind).finish(dependencyObject.get(), ref);

        return bind.finish(ref);
    }

    public final <T, C> @NotNull Optional<T> wrapVar(VarBind<T, C, ?, ?> bind) {
        return Optional.ofNullable(getVar(bind));
    }
}
