package org.comroid.varbind;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.common.iter.Span;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

import org.jetbrains.annotations.Nullable;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.comroid.common.Polyfill.deadCast;

public class VariableCarrier<BAS, OBJ extends BAS, ARR extends BAS, DEP>
        implements VarCarrier<BAS, DEP> {
    private final SeriLib<BAS, OBJ, ARR> seriLib;
    private final GroupBind<BAS, OBJ, ARR> rootBind;
    private final Map<VarBind<? extends BAS, Object, ? super DEP, ?, Object>, AtomicReference<Span<Object>>> vars = new ConcurrentHashMap<>();
    private final Map<VarBind<? extends BAS, Object, ? super DEP, ?, Object>, OutdateableReference<Object>> computed = new ConcurrentHashMap<>();
    private final DEP dependencyObject;
    private final Set<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> initiallySet;

    protected VariableCarrier(
            SeriLib<BAS, OBJ, ARR> seriLib,
            @Nullable OBJ initialData,
            @Nullable DEP dependencyObject
    ) {
        this.seriLib = seriLib;
        this.rootBind = findRootBind(getClass());
        this.initiallySet = unmodifiableSet(updateVars(seriLib.createUniObjectNode(initialData)));
        this.dependencyObject = dependencyObject;
    }

    private GroupBind<BAS, OBJ, ARR> findRootBind(Class<? extends VarCarrier> inClass) {
        final VarBind.Location location = inClass.getAnnotation(VarBind.Location.class);

        if (location == null) throw new IllegalStateException(String.format(
                "Class %s extends VariableCarrier, but does not have a %s annotation.",
                inClass.getName(),
                VarBind.Location.class.getName()
        ));

        return (GroupBind<BAS, OBJ, ARR>) ReflectionHelper.collectStaticFields(GroupBind.class,
                location.value(),
                true,
                VarBind.Root.class
        )
                .requireNonNull();
    }

    private Set<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> updateVars(
            @Nullable UniObjectNode<BAS, OBJ, Object> data
    ) {
        if (data == null) return emptySet();

        if (data.getType() != Primitive.OBJECT)
            throw new IllegalArgumentException("Object required");

        final HashSet<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> changed = new HashSet<>();

        getBindings().getChildren()
                .stream()
                .filter(bind -> data.containsKey(bind.getName()))
                .map(it -> (VarBind<? extends BAS, Object, Object, Object, Object>) (Object) it)
                .forEach(bind -> {
                    Span<Object> extract = bind.extract((UniNode) data);

                    ref(bind).set(extract);
                    compRef(bind).outdate();
                    changed.add(bind);
                });

        return unmodifiableSet(changed);
    }

    private <T> AtomicReference<Span<T>> ref(
            VarBind<? extends BAS, T, ? super DEP, ?, Object> bind
    ) {
        return deadCast(vars.computeIfAbsent((VarBind<? extends BAS, Object, ? super DEP, ?, Object>) bind,
                key -> new AtomicReference<>(Span.zeroSize())
        ));
    }

    private <T> OutdateableReference<T> compRef(
            VarBind<? extends BAS, Object, ? super DEP, ?, T> bind
    ) {
        return deadCast(vars.computeIfAbsent((VarBind<? extends BAS, Object, ? super DEP, ?, Object>) bind,
                key -> new AtomicReference<>(Span.zeroSize())
        ));
    }

    @Override
    public final GroupBind<BAS, OBJ, ?> getBindings() {
        return rootBind;
    }

    @Override
    public final Set<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> updateFrom(BAS node) {
        switch (seriLib.typeOf(node).typ) {
            case OBJECT:
                return updateVars(seriLib.createUniObjectNode((OBJ) node));
            case ARRAY:
                throw new IllegalArgumentException("Cannot update VariableCarrier from Object node");
        }

        throw new AssertionError();
    }

    @Override
    public final Set<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> initiallySet() {
        return initiallySet;
    }

    @Override
    public final <T> @Nullable T get(VarBind<? extends BAS, Object, ? super DEP, ?, T> pBind) {
        VarBind<? extends BAS, Object, ? super DEP, Object, Object> bind = (VarBind<? extends BAS, Object, ? super DEP, Object, Object>) pBind;
        OutdateableReference<T> ref = compRef(pBind);

        if (ref.isOutdated()) {
            // recompute

            AtomicReference<Span<Object>> reference = ref(bind);
            Span<Object> remapped = reference.get()
                    .stream()
                    .map(each -> bind.remap(each, dependencyObject))
                    .collect(Span.collector());
            final T yield = (T) bind.finish(remapped);
            ref.update(yield);
        }

        return ref.get();
    }
}
