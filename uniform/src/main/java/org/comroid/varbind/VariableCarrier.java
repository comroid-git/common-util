package org.comroid.varbind;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.Span;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.SerializationAdapter;
import org.comroid.uniform.data.node.UniObjectNode;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.comroid.common.Polyfill.deadCast;

public class VariableCarrier<DEP> implements VarCarrier<DEP> {
    @Internal
    public static GroupBind findRootBind(Class<? extends VarCarrier> inClass) {
        final VarBind.Location location = inClass.getAnnotation(VarBind.Location.class);

        if (location == null) throw new IllegalStateException(String.format(
                "Class %s extends VariableCarrier, but does not have a %s annotation.",
                inClass.getName(),
                VarBind.Location.class.getName()
        ));

        return ReflectionHelper.collectStaticFields(GroupBind.class,
                location.value(),
                true,
                VarBind.Root.class
        )
                .requireNonNull();
    }

    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final GroupBind rootBind;
    private final Map<VarBind<Object, ? super DEP, ?, Object>, AtomicReference<Span<Object>>> vars = new ConcurrentHashMap<>();
    private final Map<VarBind<Object, ? super DEP, ?, Object>, OutdateableReference<Object>> computed = new ConcurrentHashMap<>();
    private final DEP dependencyObject;
    private final Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet;

    protected VariableCarrier(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            @Nullable UniObjectNode initialData,
            @Nullable DEP dependencyObject
    ) {
        this.serializationAdapter = serializationAdapter;
        this.rootBind = findRootBind(getClass());
        this.initiallySet = unmodifiableSet(updateVars(initialData));
        this.dependencyObject = dependencyObject;
    }

    private Set<VarBind<Object, ? super DEP, ?, Object>> updateVars(
            @Nullable UniObjectNode data
    ) {
        if (data == null) return emptySet();

        final HashSet<VarBind<Object, ? super DEP, ?, Object>> changed = new HashSet<>();

        getRootBind().getChildren()
                .stream()
                .filter(bind -> data.has(bind.getFieldName()))
                .map(it -> (VarBind<Object, Object, Object, Object>) it)
                .forEach(bind -> {
                    Span<Object> extract = bind.extract(data);

                    ref(bind).set(extract);
                    compRef(bind).outdate();
                    changed.add(bind);
                });

        return unmodifiableSet(changed);
    }

    private <T> AtomicReference<Span<T>> ref(
            VarBind<T, ? super DEP, ?, Object> bind
    ) {
        return deadCast(vars.computeIfAbsent((VarBind<Object, ? super DEP, ?, Object>) bind,
                key -> new AtomicReference<>(Span.zeroSize())
        ));
    }

    private <T> OutdateableReference<T> compRef(
            VarBind<Object, ? super DEP, ?, T> bind
    ) {
        return deadCast(vars.computeIfAbsent((VarBind<Object, ? super DEP, ?, Object>) bind,
                key -> new AtomicReference<>(Span.zeroSize())
        ));
    }

    @Override
    public final GroupBind getRootBind() {
        return rootBind;
    }

    @Override
    public Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node) {
        return unmodifiableSet(updateVars(node));
    }

    @Override
    public Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet() {
        return initiallySet;
    }

    @Override
    public final <T> @Nullable T get(VarBind<?, ? super DEP, ?, T> pBind) {
        VarBind<Object, ? super DEP, Object, T> bind = (VarBind<Object, ? super DEP, Object, T>) pBind;
        OutdateableReference<T> ref = (OutdateableReference<T>) compRef(bind);

        if (ref.isOutdated()) {
            // recompute

            AtomicReference<Span<Object>> reference = ref(Polyfill.deadCast(bind));
            final T yield = bind.process(dependencyObject, reference.get());
            ref.update(yield);
        }

        return ref.get();
    }
}