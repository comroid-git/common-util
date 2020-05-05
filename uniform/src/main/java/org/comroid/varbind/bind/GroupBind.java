package org.comroid.varbind.bind;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;
import org.comroid.varbind.container.DataContainer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GroupBind<T extends DataContainer<? super D>, D> {
    private static final BiFunction<UniObjectNode, String, UniObjectNode> objectNodeExtractor = (node, sub) -> node.get(sub)
            .asObjectNode();
    final List<? extends VarBind<?, D, ?, ?>> children = new ArrayList<>();
    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final String groupName;
    private final @Nullable GroupBind<? super T, D> parent;
    private final List<GroupBind<? extends T, D>> subgroups = new ArrayList<>();
    private final @Nullable Invocable<? extends T> constructor;

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter, String groupName
    ) {
        this(serializationAdapter, groupName, null);
    }

    public GroupBind(
            SerializationAdapter<?, ?, ?> serializationAdapter, String groupName, Invocable<? extends T> invocable
    ) {
        this(null, serializationAdapter, groupName, invocable);
    }

    private GroupBind(
            @Nullable GroupBind<? super T, D> parent,
            SerializationAdapter<?, ?, ?> serializationAdapter,
            String groupName,
            @Nullable Invocable<? extends T> invocable
    ) {
        this.parent = parent;
        this.serializationAdapter = serializationAdapter;
        this.groupName = groupName;
        this.constructor = invocable;
    }

    @Override
    public String toString() {
        return String.format("GroupBind{groupName='%s', parent=%s}", groupName, parent);
    }

    public Optional<GroupBind<? extends T, D>> findGroupForData(UniObjectNode data) {
        if (isValidData(data)) {
            if (subgroups.isEmpty())
                return Optional.of(this);

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
        if (parent != null)
            return false;

        return streamAllChildren().allMatch(bind -> data.has(bind.getFieldName()) || bind.isOptional());
    }

    public Stream<? extends VarBind<?, D, ?, ?>> streamAllChildren() {
        return Stream.concat(children.stream(), getParent()
                .map(GroupBind::streamAllChildren)
                .orElseGet(Stream::empty));
    }

    public List<? extends VarBind<?, D, ?, ?>> getDirectChildren() {
        return Collections.unmodifiableList(children);
    }

    public Optional<Invocable<? extends T>> getConstructor() {
        return Optional.ofNullable(constructor);
    }

    public String getName() {
        return groupName;
    }

    public Optional<GroupBind<? super T, D>> getParent() {
        return Optional.ofNullable(parent);
    }

    public Collection<GroupBind<? extends T, D>> getSubgroups() {
        return subgroups;
    }

    public Invocable<T> autoConstructor(
            Class<T> resultType, Class<D> dependencyType
    ) {
        final Class<?>[] typesUnordered = {
                UniObjectNode.class, SerializationAdapter.class, serializationAdapter.objectType.typeClass(), dependencyType
        };

        return Invocable.ofConstructor(resultType, typesUnordered);
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName) {
        return subGroup(subGroupName, null);
    }

    public <R extends T> GroupBind<R, D> subGroup(String subGroupName, Invocable<? extends R> constructor) {
        final GroupBind<R, D> groupBind = new GroupBind<>(this, serializationAdapter, subGroupName, constructor);
        subgroups.add(groupBind);
        return groupBind;
    }

    public final <A> VarBind.OneStage<A> bind1stage(String fieldname, UniValueNode.ValueType<A> type) {
        return bind1stage(fieldname, extractor(type));
    }

    public final <A> VarBind.OneStage<A> bind1stage(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor
    ) {
        return new VarBind.OneStage<>(this, fieldName, extractor);
    }

    private <A> BiFunction<UniObjectNode, String, A> extractor(final UniValueNode.ValueType<A> type) {
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
            String fieldName, UniValueNode.ValueType<A> type, Function<A, R> remapper
    ) {
        return bind2stage(fieldName, extractor(type), remapper);
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
            String fieldName, UniValueNode.ValueType<A> type, BiFunction<D, A, R> resolver
    ) {
        return bindDependent(fieldName, extractor(type), resolver);
    }

    public final <A, C extends Collection<A>> ArrayBind.OneStage<A, C> list1stage(
            String fieldName, UniValueNode.ValueType<A> type, Supplier<C> collectionSupplier
    ) {
        return list1stage(fieldName, eachExtractor(type), collectionSupplier);
    }

    public final <A, C extends Collection<A>> ArrayBind.OneStage<A, C> list1stage(
            String fieldName, Function<? extends UniNode, A> extractor, Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.OneStage<>(this, fieldName, extractor, collectionSupplier);
    }

    private <A> Function<UniNode, A> eachExtractor(final UniValueNode.ValueType<A> type) {
        return root -> root.as(type);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.TwoStage<A, R, C> list2stage(
            String fieldName, UniValueNode.ValueType<A> type, Function<A, R> remapper, Supplier<C> collectionSupplier
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
            String fieldName, UniValueNode.ValueType<A> type, BiFunction<D, A, R> resolver, Supplier<C> collectionSupplier
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
