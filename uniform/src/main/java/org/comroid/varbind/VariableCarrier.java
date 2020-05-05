package org.comroid.varbind;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Processor;
import org.comroid.common.iter.Span;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.ref.Reference;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.node.UniObjectNode;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.comroid.common.Polyfill.uncheckedCast;

@SuppressWarnings("unchecked")
public class VariableCarrier<DEP> implements VarCarrier<DEP> {
    private final GroupBind<?, DEP>                                                                   rootBind;
    private final Map<VarBind<Object, ? super DEP, ?, Object>, AtomicReference<Span<Object>>> vars     =
            new ConcurrentHashMap<>();
    private final Map<VarBind<Object, ? super DEP, ?, Object>, OutdateableReference<Object>>  computed
                                                                                                       =
            new ConcurrentHashMap<>();
    private final DEP                                                                         dependencyObject;
    private final Set<VarBind<Object, ? super DEP, ?, Object>>                                initiallySet;
    private final Class<? extends VarCarrier<? super DEP>>                                    myType;

    public VariableCarrier(
            @Nullable UniObjectNode initialData
    ) {
        this(initialData, null);
    }

    public VariableCarrier(
            @Nullable UniObjectNode initialData, @Nullable DEP dependencyObject
    ) {
        this(initialData, dependencyObject, null);
    }

    public VariableCarrier(
            @Nullable UniObjectNode initialData,
            @Nullable DEP dependencyObject,
            @Nullable Class<? extends VarCarrier<DEP>> containingClass
    ) {
        this.myType           = containingClass == null ? (Class<? extends VarCarrier<? super DEP>>) getClass() : containingClass;
        this.rootBind         = findRootBind(myType);
        this.initiallySet     = unmodifiableSet(updateVars(initialData));
        this.dependencyObject = dependencyObject;
    }

    private Set<VarBind<Object, ? super DEP, ?, Object>> updateVars(
            @Nullable UniObjectNode data
    ) {
        if (data == null) {
            return emptySet();
        }

        final HashSet<VarBind<Object, ? super DEP, ?, Object>> changed = new HashSet<>();

        getRootBind().streamAllChildren()
                .filter(bind -> !(bind instanceof VarBind.NotAutoprocessed))
                .map(it -> (VarBind<Object, ? super DEP, ?, Object>) it)
                .filter(bind -> data.has(bind.getFieldName()))
                .map(it -> (VarBind<Object, Object, Object, Object>) it)
                .forEach(bind -> {
                    Span<Object> extract = bind.extract(data);

                    extrRef(bind).set(extract);
                    compRef(bind).outdate();
                    changed.add(bind);
                });

        return unmodifiableSet(changed);
    }

    @Override
    public final GroupBind<?, DEP> getRootBind() {
        return rootBind;
    }

    private <T> OutdateableReference<T> compRef(
            VarBind<Object, ? super DEP, ?, T> bind
    ) {
        return uncheckedCast(computed.computeIfAbsent((VarBind<Object, ? super DEP, ?, Object>) bind,
                key -> new OutdateableReference<>()
        ));
    }

    @Override
    public final Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node) {
        return unmodifiableSet(updateVars(node));
    }

    @Override
    public final Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet() {
        return initiallySet;
    }

    @Override
    public final <T> Optional<Reference<T>> getByName(String name) {
        final String[] split = name.split("\\.");

        if (split.length == 1) {
            return getRootBind().streamAllChildren()
                    .filter(bind -> bind.getFieldName()
                            .equals(name))
                    .findAny()
                    .map(it -> ref(uncheckedCast(it)));
        }

        // any stage in the groupbind tree
        Processor<GroupBind<?, DEP>> parentGroup = Processor.ofConstant(getRootBind());

        // find the topmost parent
        while (parentGroup.requireNonNull()
                .getParent()
                .isPresent()) {
            parentGroup = parentGroup.map(group -> group.getParent()
                    .orElse(Polyfill.uncheckedCast(group)));
        }

        // find the subgroup named the first split part,
        return parentGroup.flatMap(parent -> parent.getSubgroups()
                .stream())
                .filter(group -> group.getName()
                        .equals(split[0]))
                // then find the subgroup named second split part
                .flatMap(GroupBind::streamAllChildren)
                .filter(bind -> bind.getFieldName()
                        .equals(split[1]))
                .findAny()
                // get reference of bind
                .map(it -> ref(uncheckedCast(it)));
    }

    @Override
    public final @NotNull <T> OutdateableReference<T> ref(VarBind<?, ? super DEP, ?, T> pBind) {
        VarBind<Object, ? super DEP, Object, T> bind = (VarBind<Object, ? super DEP, Object, T>) pBind;
        OutdateableReference<T>                 ref  = compRef(bind);

        if (ref.isOutdated()) {
            // recompute

            AtomicReference<Span<Object>> reference = extrRef(uncheckedCast(bind));
            final T                       yield     = bind.process(dependencyObject, reference.get());
            ref.update(yield);
        }

        return ref;
    }

    @Override
    public final DEP getDependencyObject() {
        return dependencyObject;
    }

    @Override
    public UniObjectNode toObjectNode() {
        return null; // todo
    }

    VariableCarrier(
            Map<VarBind<Object, DEP, ?, Object>, Object> initialValues,
            DEP dependencyObject,
            Class<? extends VarCarrier<? super DEP>> containingClass
    ) {
        this.myType           = containingClass == null ? (Class<? extends VarCarrier<? super DEP>>) getClass() : containingClass;
        this.rootBind         = findRootBind(myType);
        this.initiallySet     = unmodifiableSet(initialValues.keySet());
        this.dependencyObject = dependencyObject;
        initialValues.forEach((bind, value) -> extrRef(bind).set(Span.singleton(value)));
    }

    @Internal
    public static <T extends VarCarrier<? super D>, D> GroupBind<T, D> findRootBind(Class<T> inClass) {
        final VarBind.Location location = ReflectionHelper.findAnnotation(VarBind.Location.class, inClass, ElementType.TYPE)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Class %s extends VariableCarrier,\nbut does not have a %s annotation.",
                        inClass.getName(),
                        VarBind.Location.class.getName()
                )));

        return ReflectionHelper.collectStaticFields(GroupBind.class, location.value(), true, VarBind.Root.class)
                .requireNonNull();
    }

    private <T> AtomicReference<Span<T>> extrRef(
            VarBind<T, ? super DEP, ?, Object> bind
    ) {
        return uncheckedCast(vars.computeIfAbsent((VarBind<Object, ? super DEP, ?, Object>) bind,
                key -> new AtomicReference<>(Span.zeroSize())
        ));
    }

    public Class<? extends VarCarrier<? super DEP>> getRepresentedType() {
        return myType;
    }
}
