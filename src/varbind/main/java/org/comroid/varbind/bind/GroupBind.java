package org.comroid.varbind.bind;

import org.comroid.api.Invocable;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.span.Span;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.container.DataContainer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GroupBind<T extends DataContainer<? extends D>, D> {
    private static final BiFunction<UniObjectNode, String, UniObjectNode> objectNodeExtractor = (node, sub) -> node.get(sub)
            .asObjectNode();
    final List<? extends VarBind<?, D, ?, ?>> children = new ArrayList<>();
    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final String groupName;
    private final Span<GroupBind<? super T, D>> parents;
    private final List<GroupBind<? extends T, D>> subgroups = new ArrayList<>();
    private final @Nullable Invocable<? extends T> constructor;

    public List<? extends VarBind<?, D, ?, ?>> getDirectChildren() {
        return Collections.unmodifiableList(children);
    }

    public Optional<Invocable<? extends T>> getConstructor() {
        return Optional.ofNullable(constructor);
    }

    public String getName() {
        return groupName;
    }

    public Span<GroupBind<? super T, D>> getParents() {
        return parents;
    }

    public Collection<GroupBind<? extends T, D>> getSubgroups() {
        return subgroups;
    }

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName
    ) {
        this(serializationAdapter, groupName, (Invocable<T>) null);
    }

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName,
            Class<? extends T> constructorClass
    ) {
        this(serializationAdapter, groupName, Invocable.ofConstructor(constructorClass));
    }

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName,
            Invocable<? extends T> invocable
    ) {
        this(Span.empty(), serializationAdapter, groupName, invocable);
    }

    private GroupBind(
            GroupBind<? super T, D> parents,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName,
            @Nullable Invocable<? extends T> invocable
    ) {
        this(
                Span.singleton(Objects.requireNonNull(parents, "parents")),
                serializationAdapter,
                groupName,
                invocable
        );
    }

    private GroupBind(
            Span<GroupBind<? super T, D>> parents,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName,
            @Nullable Invocable<? extends T> invocable
    ) {
        this.parents = parents;
        this.serializationAdapter = serializationAdapter;
        this.groupName = groupName;
        this.constructor = invocable;
    }

    private static <T extends DataContainer<? extends D>, D> GroupBind<? super T, D> findRootParent(Collection<GroupBind<? super T, D>> groups) {
        if (groups.size() == 0)
            throw new AssertionError();

        if (groups.size() == 1)
            return groups.iterator().next();

        //noinspection RedundantCast -> false positive; todo: wtf is this
        return (GroupBind<? super T, D>) findRootParent(groups.stream()
                .map(GroupBind::getParents)
                .flatMap(Collection::stream)
                .map(it -> (GroupBind<? super T, D>) it)
                .collect(Collectors.toSet()));
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataContainer<? extends D>, D> GroupBind<T, D> combine(String groupName, GroupBind<?, D>... parents) {
        return combine(groupName, null, parents);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataContainer<? extends D>, D> GroupBind<T, D> combine(
            String groupName,
            Invocable<? extends T> invocable,
            GroupBind<?, D>... parents
    ) {
        final GroupBind<?, D> rootParent = (GroupBind<?, D>) findRootParent(Polyfill.uncheckedCast(Arrays.asList(parents)));

        // god is never gonna forgive me for this

        return new GroupBind<>(Polyfill.<GroupBind<? super T, D>>uncheckedCast(Span.immutable(parents)),
                rootParent.serializationAdapter, groupName, invocable);
    }

    @Override
    public String toString() {
        return String.format("GroupBind{groupName='%s', parent=%s}", groupName, parents);
    }

    public Optional<GroupBind<? extends T, D>> findGroupForData(UniObjectNode data) {
        if (isValidData(data)) {
            if (subgroups.isEmpty())
                return Optional.of(this);

            //noinspection rawtypes
            GroupBind[] fitting = subgroups.stream()
                    .filter(group -> group.isValidData(data))
                    .toArray(GroupBind[]::new);
            if (fitting.length == 1)
                //noinspection unchecked
                return (Optional<GroupBind<? extends T, D>>) fitting[0].findGroupForData(data);

            throw new UnsupportedOperationException("Too many fitting subgroups found: " + Arrays.toString(fitting));
        } else return Optional.empty();
    }

    public boolean isValidData(UniObjectNode data) {
        if (!parents.isEmpty())
            return false;

        return streamAllChildren().allMatch(bind -> data.has(bind.getFieldName()) || !bind.isRequired());
    }

    public Stream<? extends VarBind<?, D, ?, ?>> streamAllChildren() {
        return Stream.concat(children.stream(), getParents()
                .stream()
                .flatMap(GroupBind::streamAllChildren)
        ).distinct();
    }

    public Invocable<? super T> autoConstructor(
            Class<T> resultType, Class<D> dependencyType
    ) {
        final Class<?>[] typesUnordered = {
                UniObjectNode.class, SerializationAdapter.class, serializationAdapter.objectType.typeClass(), dependencyType
        };

        return Invocable.ofConstructor(resultType, typesUnordered);
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName) {
        return subGroup(subGroupName, (Invocable<R>) null);
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName, Class<? extends T> type) {
        return subGroup(subGroupName, Polyfill.<Invocable<R>>uncheckedCast(Invocable.ofConstructor(type)));
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName, Constructor<? extends T> type) {
        return subGroup(subGroupName, Polyfill.<Invocable<R>>uncheckedCast(Invocable.ofConstructor(type)));
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName, Invocable<? extends R> constructor) {
        final GroupBind<R, D> groupBind = new GroupBind<>(this, serializationAdapter, subGroupName, constructor);
        subgroups.add(groupBind);
        return groupBind;
    }

    public BindBuilder<?, D, ?, ?> createBind(String fieldName) {
        return new BindBuilder<>(this, fieldName);
    }

    @Internal
    public void addChild(VarBind<?, ? super D, ?, ?> child) {
        children.add(Polyfill.uncheckedCast(child));
    }
}
