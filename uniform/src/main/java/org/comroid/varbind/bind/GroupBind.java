package org.comroid.varbind.bind;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.iter.Span;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.container.DataContainer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
    private final @Nullable Invocable<? super T> constructor;

    public List<? extends VarBind<?, D, ?, ?>> getDirectChildren() {
        return Collections.unmodifiableList(children);
    }

    public Optional<Invocable<? super T>> getConstructor() {
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
            SerializationAdapter<?, ?, ?> serializationAdapter, String groupName
    ) {
        this(serializationAdapter, groupName, (Invocable<T>) null);
    }

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter, String groupName, Class<T> constructorClass
    ) {
        this(serializationAdapter, groupName, Invocable.ofConstructor(constructorClass));
    }

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter, String groupName, Invocable<? super T> invocable
    ) {
        this(Span.empty(), serializationAdapter, groupName, invocable);
    }

    private GroupBind(
            GroupBind<? super T, D> parents,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName,
            @Nullable Invocable<? super T> invocable
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
            @Nullable Invocable<? super T> invocable
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
    public static <T extends DataContainer<? extends D>, D> GroupBind<T, D> combine(String groupName, Invocable<? super T> invocable, GroupBind<?, D>... parents) {
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

        return streamAllChildren().allMatch(bind -> data.has(bind.getFieldName()) || bind.isOptional());
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

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName, Constructor<? extends T> type) {
        return subGroup(subGroupName, Polyfill.<Invocable<R>>uncheckedCast(Invocable.ofConstructor(type)));
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName, Invocable<? super R> constructor) {
        final GroupBind<R, D> groupBind = new GroupBind<>(this, serializationAdapter, subGroupName, constructor);
        subgroups.add(groupBind);
        return groupBind;
    }

    public final <A> VarBind.OneStage<A> bind1stage(String fieldname, ValueType<A> type) {
        return bind1stage(fieldname, extractor(type));
    }

    public final <A> VarBind.OneStage<A> bind1stage(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor
    ) {
        return new VarBind.OneStage<>(this, fieldName, extractor);
    }

    private <A> BiFunction<UniObjectNode, String, A> extractor(final ValueType<A> type) {
        return (root, fieldName) -> root.get(fieldName)
                .as(type);
    }

    public final <R> VarBind.TwoStage<UniObjectNode, R> bind2stage(
            String fieldName, Function<UniObjectNode, R> remapper
    ) {
        return bind2stage(fieldName, objectNodeExtractor, remapper);
    }

    public final <A, R> VarBind.TwoStage<A, R> bind2stage(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor, Function<A, R> remapper
    ) {
        return new VarBind.TwoStage<>(this, fieldName, extractor, remapper);
    }

    public final <A, R> VarBind.TwoStage<A, R> bind2stage(
            String fieldName, ValueType<A> type, Function<A, R> remapper
    ) {
        return bind2stage(fieldName, extractor(type), remapper);
    }

    public final <R extends DataContainer<D>> VarBind.DependentTwoStage<UniObjectNode, D, R> bindDependent(
        String fieldName,
        GroupBind<R, D> group
    ) {
        return bindDependent(fieldName,group.getConstructor()
                .map(it -> Polyfill.<BiFunction<D, UniObjectNode, R>>uncheckedCast(it.<D, UniObjectNode>biFunction()))
                .orElseThrow(() -> new NoSuchElementException("No Constructor available for GroupBind " + group)));
    }

    public final <R> VarBind.DependentTwoStage<UniObjectNode, D, R> bindDependent(
            String fieldName, BiFunction<D, UniObjectNode, R> resolver
    ) {
        return bindDependent(fieldName, objectNodeExtractor, resolver);
    }

    public final <A, R> VarBind.DependentTwoStage<A, D, R> bindDependent(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor, BiFunction<D, A, R> resolver
    ) {
        return new VarBind.DependentTwoStage<>(this, fieldName, extractor, resolver);
    }

    public final <A, R> VarBind.DependentTwoStage<A, D, R> bindDependent(
            String fieldName, ValueType<A> type, BiFunction<D, A, R> resolver
    ) {
        return bindDependent(fieldName, extractor(type), resolver);
    }

    public final <A, C extends Collection<A>> ArrayBind.OneStage<A, C> list1stage(
            String fieldName, ValueType<A> type, Supplier<C> collectionSupplier
    ) {
        return list1stage(fieldName, eachExtractor(type), collectionSupplier);
    }

    public final <A, C extends Collection<A>> ArrayBind.OneStage<A, C> list1stage(
            String fieldName, Function<? extends UniNode, A> extractor, Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.OneStage<>(this, fieldName, extractor, collectionSupplier);
    }

    private <A> Function<UniNode, A> eachExtractor(final ValueType<A> type) {
        return root -> root.as(type);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.TwoStage<A, R, C> list2stage(
            String fieldName, ValueType<A> type, Function<A, R> remapper, Supplier<C> collectionSupplier
    ) {
        return list2stage(fieldName, eachExtractor(type), remapper, collectionSupplier);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.TwoStage<A, R, C> list2stage(
            String fieldName, Function<? extends UniNode, A> extractor, Function<A, R> remapper, Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.TwoStage<>(this, fieldName, extractor, remapper, collectionSupplier);
    }

    public final <R, C extends Collection<R>> ArrayBind.TwoStage<UniObjectNode, R, C> list2stage(
            String fieldName, Function<UniObjectNode, R> remapper, Supplier<C> collectionSupplier
    ) {
        return list2stage(fieldName, UniNode::asObjectNode, remapper, collectionSupplier);
    }

    public final <R extends DataContainer<D>, C extends Collection<R>> ArrayBind.DependentTwoStage<UniObjectNode, D, R, C> listDependent(
        String fieldName, GroupBind<R, D> group, Supplier<C> collectionSupplier
) {
        return listDependent(fieldName, group.getConstructor()
                .map(it -> Polyfill.<BiFunction<D, UniObjectNode, R>>uncheckedCast(it.<D, UniObjectNode>biFunction()))
                .orElseThrow(() -> new NoSuchElementException("No Constructor available for GroupBind " + group)),
                collectionSupplier);
    }

    public final <R, C extends Collection<R>> ArrayBind.DependentTwoStage<UniObjectNode, D, R, C> listDependent(
            String fieldName, BiFunction<D, UniObjectNode, R> resolver, Supplier<C> collectionSupplier
    ) {
        return listDependent(fieldName, UniNode::asObjectNode, resolver, collectionSupplier);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.DependentTwoStage<A, D, R, C> listDependent(
            String fieldName,
            Function<? extends UniNode, A> extractor,
            BiFunction<D, A, R> resolver,
            Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.DependentTwoStage<>(this, fieldName, extractor, resolver, collectionSupplier);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.DependentTwoStage<A, D, R, C> listDependent(
            String fieldName, ValueType<A> type, BiFunction<D, A, R> resolver, Supplier<C> collectionSupplier
    ) {
        return listDependent(fieldName, eachExtractor(type), resolver, collectionSupplier);
    }

    private <A> BiFunction<UniObjectNode, String, A> extractor(final Class<A> extractTarget) {
        return (node, fieldName) -> extractTarget.cast(node.get(fieldName)
                .asRaw(null));
    }

    private <A> BiFunction<UniArrayNode, String, Collection<A>> splitExtractor(
            BiFunction<UniObjectNode, String, A> dataExtractor
    ) {
        return (arrayNode, fieldName) -> arrayNode.asNodeList()
                .stream()
                .map(UniNode::asObjectNode)
                .filter(node -> !node.isNull())
                .map(node -> dataExtractor.apply(node, fieldName))
                .collect(Collectors.toList());
    }

    @Internal
    public void addChild(VarBind<?, ? super D, ?, ?> child) {
        children.add(Polyfill.uncheckedCast(child));
    }
}
