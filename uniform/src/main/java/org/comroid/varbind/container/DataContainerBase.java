package org.comroid.varbind.container;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Processor;
import org.comroid.common.iter.Span;
import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.ref.Reference;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.annotation.Location;
import org.comroid.varbind.annotation.RootBind;
import org.comroid.varbind.bind.ArrayBind;
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
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.comroid.common.Polyfill.uncheckedCast;

@SuppressWarnings("unchecked")
public class DataContainerBase<DEP> implements DataContainer<DEP> {
    private final GroupBind<? extends DataContainer<DEP>, DEP> rootBind;
    private final Map<String, Span<VarBind<?, ? super DEP, ?, ?>>> binds = TrieMap.ofString();
    private final Map<String, Reference.Settable<Span<Object>>> vars = TrieMap.ofString();
    private final Map<String, OutdateableReference<Object>> computed = TrieMap.ofString();
    private final DEP dependencyObject;
    private final Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet;
    private final Class<? extends DataContainer<DEP>> myType;

    @Override
    public final GroupBind<?, DEP> getRootBind() {
        return rootBind;
    }

    @Override
    public final DEP getDependent() {
        return dependencyObject;
    }

    @Override
    public Class<? extends DataContainer<? super DEP>> getRepresentedType() {
        return myType;
    }

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
        this.myType = containingClass == null ? (Class<? extends DataContainer<DEP>>) getClass() : containingClass;
        this.rootBind = findRootBind(myType);
        this.initiallySet = unmodifiableSet(updateVars(initialData));
        this.dependencyObject = dependencyObject;
    }

    DataContainerBase(
            Map<VarBind<Object, DEP, ?, Object>, Object> initialValues,
            DEP dependencyObject,
            Class<? extends DataContainer<DEP>> containingClass
    ) {
        this.myType = containingClass == null ? (Class<? extends DataContainer<DEP>>) getClass() : containingClass;
        this.rootBind = findRootBind(myType);
        this.initiallySet = unmodifiableSet(initialValues.keySet());
        this.dependencyObject = dependencyObject;
        initialValues.forEach((bind, value) -> getExtractionReference(bind).set(Span.singleton(value)));
    }

    @Internal
    public static <T extends DataContainer<? extends D>, D> GroupBind<T, D> findRootBind(Class<T> inClass) {
        final Location location = ReflectionHelper.findAnnotation(Location.class, inClass, ElementType.TYPE)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Class %s extends VariableCarrier,\nbut does not have a %s annotation.",
                        inClass.getName(),
                        Location.class.getName()
                )));

        return ReflectionHelper
                .<GroupBind<T, D>>collectStaticFields(
                        Polyfill.uncheckedCast(GroupBind.class),
                        location.value(),
                        true,
                        RootBind.class
                ).requireNonNull(String.format("No @RootBind annotated field found in %s", location.value()));
    }

    private Set<VarBind<Object, ? super DEP, ?, Object>> updateVars(
            @Nullable UniObjectNode data
    ) {
        if (data == null) {
            return emptySet();
        }

        if (!rootBind.isValidData(data))
            throw new IllegalArgumentException("Data is invalid");

        final HashSet<VarBind<Object, ? super DEP, ?, Object>> changed = new HashSet<>();

        getRootBind().streamAllChildren()
                .filter(bind -> !(bind instanceof Reprocessed))
                .map(it -> (VarBind<Object, ? super DEP, ?, Object>) it)
                .filter(bind -> data.has(bind.getFieldName()))
                .map(it -> (VarBind<Object, Object, Object, Object>) it)
                .forEach(bind -> {
                    Span<Object> extract = bind.extract(data);

                    getExtractionReference(bind).set(extract);
                    // do not compute reference at first
                    //getComputedReference(bind).update(bind.finish(extract));
                    changed.add(bind);
                });

        return unmodifiableSet(changed);
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
                    .map(it -> getComputedReference(uncheckedCast(it)));
        }

        // any stage in the groupbind tree
        Processor<GroupBind<?, DEP>> parentGroup = Processor.ofConstant(getRootBind());

        // find the topmost parent
        while (parentGroup.requireNonNull()
                .getParents().isSingle()) {
            parentGroup = parentGroup.map(group -> group.getParents()
                    .wrap()
                    .orElse(Polyfill.uncheckedCast(group)));
        }

        // find the subgroup named the first split part,
        return parentGroup.into(parent -> parent.getSubgroups().stream())
                .filter(group -> group.getName().equals(split[0]))
                // then find the subgroup named second split part
                .flatMap(GroupBind::streamAllChildren)
                .filter(bind -> bind.getFieldName().equals(split[1]))
                .findAny()
                // get reference of bind
                .map(it -> getComputedReference(uncheckedCast(it)));
    }

    @Override
    public UniObjectNode toObjectNode(SerializationAdapter<?, ?, ?> serializationAdapter) {
        final UniObjectNode data = serializationAdapter.createUniObjectNode(null);

        binds.keySet().forEach(key -> {
            final @NotNull Span<Object> them = getExtractionReference(key).requireNonNull("Span is null");

            if (them.isEmpty())
                return;

            if (them.isSingle()) {
                final Object it = them.requireNonNull("AssertionFailure");

                if (it instanceof DataContainer)
                    data.putObject()
            }
        });

        return data;
    }

    @Override
    public <R, T> @Nullable R put(VarBind<T, ? super DEP, ?, R> bind, Function<R, T> parser, R value) {
        final T apply = parser.apply(value);
        final R prev = getComputedReference(bind).get();

        if (bind instanceof ArrayBind) {
            getExtractionReference(bind).compute(span -> {
                span.add(apply);
                return span;
            });
            getComputedReference(bind).update(value);
        } else {
            getExtractionReference(bind).set(Span.singleton(apply));
            getComputedReference(bind).update(value);
        }

        return prev;
    }

    @Override
    public <E> Reference.Settable<Span<E>> getExtractionReference(String fieldName) {
        return Polyfill.uncheckedCast(vars.computeIfAbsent(fieldName,
                key -> Reference.Settable.create(new Span<>())));
    }

    @Override
    public <T, E> OutdateableReference<T> getComputedReference(VarBind<E, ? super DEP, ?, T> bind) {
        return Polyfill.uncheckedCast(computed.computeIfAbsent(cacheBind(bind),
                key -> Polyfill.uncheckedCast(new ComputedReference<>(bind))));
    }

    @Override
    public <T> String cacheBind(VarBind<?, ? super DEP, ?, ?> bind) {
        final String fieldName = bind.getFieldName();
        final Span<VarBind<?, ? super DEP, ?, ?>> span = binds.computeIfAbsent(fieldName, key -> new Span<>());

        span.add(bind);
        return fieldName;
    }

    public class ComputedReference<T, E> extends OutdateableReference<T> {
        private final VarBind<E, ? super DEP, ?, T> bind;
        private final Processor<T> accessor;

        public ComputedReference(VarBind<E, ? super DEP, ?, T> bind) {
            this.bind = bind;
            this.accessor = getExtractionReference(bind)
                    .process()
                    .map(extr -> this.bind.process(getDependent(), extr));
        }

        @Override
        public final @Nullable T get() {
            if (!isOutdated())
                return super.get();
            return update(accessor.get());
        }
    }
}
