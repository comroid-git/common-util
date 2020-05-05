package org.comroid.varbind.container;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Processor;
import org.comroid.common.iter.Span;
import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.ref.Reference;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.Root;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.model.Reprocessed;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.comroid.common.Polyfill.uncheckedCast;

@SuppressWarnings("unchecked")
public class DataContainerBase<DEP> implements DataContainer<DEP> {
    private final GroupBind<?, DEP> rootBind;
    private final Map<VarBind<?, ? super DEP, ?, ?>, String> binds = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Span<Object>>> vars = TrieMap.ofString();
    private final Map<String, OutdateableReference<Object>> computed = TrieMap.ofString();
    private final DEP dependencyObject;
    private final Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet;
    private final Class<? extends DataContainer<? super DEP>> myType;

    public DataContainerBase(
            @Nullable UniObjectNode initialData
    ) {
        this(initialData, null);
    }

    public DataContainerBase(
            @Nullable UniObjectNode initialData, @Nullable DEP dependencyObject
    ) {
        this(initialData, dependencyObject, null);
    }

    public DataContainerBase(
            @Nullable UniObjectNode initialData,
            @Nullable DEP dependencyObject,
            @Nullable Class<? extends DataContainer<DEP>> containingClass
    ) {
        this.myType = containingClass == null ? (Class<? extends DataContainer<? super DEP>>) getClass() : containingClass;
        this.rootBind = findRootBind(myType);
        this.initiallySet = unmodifiableSet(updateVars(initialData));
        this.dependencyObject = dependencyObject;
    }

    DataContainerBase(
            Map<VarBind<Object, DEP, ?, Object>, Object> initialValues,
            DEP dependencyObject,
            Class<? extends DataContainer<? super DEP>> containingClass
    ) {
        this.myType = containingClass == null ? (Class<? extends DataContainer<? super DEP>>) getClass() : containingClass;
        this.rootBind = findRootBind(myType);
        this.initiallySet = unmodifiableSet(initialValues.keySet());
        this.dependencyObject = dependencyObject;
        initialValues.forEach((bind, value) -> extrRef(bind).set(Span.singleton(value)));
    }

    @Internal
    public static <T extends DataContainer<? super D>, D> GroupBind<T, D> findRootBind(Class<T> inClass) {
        final Location location = ReflectionHelper.findAnnotation(Location.class, inClass, ElementType.TYPE)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Class %s extends VariableCarrier,\nbut does not have a %s annotation.",
                        inClass.getName(),
                        Location.class.getName()
                )));

        return ReflectionHelper.collectStaticFields(GroupBind.class, location.value(), true, Root.class)
                .requireNonNull();
    }

    private Set<VarBind<Object, ? super DEP, ?, Object>> updateVars(
            @Nullable UniObjectNode data
    ) {
        if (data == null) {
            return emptySet();
        }

        final HashSet<VarBind<Object, ? super DEP, ?, Object>> changed = new HashSet<>();

        getRootBind().streamAllChildren()
                .filter(bind -> !(bind instanceof Reprocessed))
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

    @Override
    public final @NotNull <T> OutdateableReference<T> ref(VarBind<?, ? super DEP, ?, T> pBind) {
        VarBind<Object, ? super DEP, Object, T> bind = (VarBind<Object, ? super DEP, Object, T>) pBind;
        OutdateableReference<T> ref = compRef(bind);

        if (ref.isOutdated()) {
            // recompute

            AtomicReference<Span<Object>> reference = extrRef(uncheckedCast(bind));
            final T yield = bind.process(dependencyObject, reference.get());
            ref.update(yield);
        }

        return ref;
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
    public final DEP getDependencyObject() {
        return dependencyObject;
    }

    @Override
    public UniObjectNode toObjectNode() {
        return null; // todo
    }

    @Override
    public Class<? extends DataContainer<? super DEP>> getRepresentedType() {
        return myType;
    }

    private <T> AtomicReference<Span<T>> extrRef(
            VarBind<T, ? super DEP, ?, Object> bind
    ) {
        return uncheckedCast(vars.computeIfAbsent(fieldName(bind), key -> new AtomicReference<>(Span.zeroSize())));
    }

    private <T> OutdateableReference<T> compRef(
            VarBind<Object, ? super DEP, ?, T> bind
    ) {
        return uncheckedCast(computed.computeIfAbsent(fieldName(bind), key -> new OutdateableReference<>()));
    }

    private <T> String fieldName(VarBind<?, ? super DEP, ?, ?> bind) {
        return binds.computeIfAbsent(bind, VarBind::getFieldName);
    }
}
