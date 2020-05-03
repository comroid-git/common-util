package org.comroid.varbind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;

import org.jetbrains.annotations.Nullable;

public final class GroupBind<T extends VarCarrier<? super D>, D> {
    final                   List<? extends VarBind<?, ?, ?, ?>> children  = new ArrayList<>();
    private final           SerializationAdapter<?, ?, ?>       serializationAdapter;
    private final           String                              groupName;
    private final @Nullable GroupBind<? super T, D>             parent;
    private final           List<GroupBind<? extends T, D>>     subgroups = new ArrayList<>();
    private final @Nullable Invocable<? extends T>              constructor;

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
        this.parent               = parent;
        this.serializationAdapter = serializationAdapter;
        this.groupName            = groupName;
        this.constructor          = invocable;

        if (parent != null) {
            parent.getChildren()
                    .forEach(it -> children.add(Polyfill.uncheckedCast(it)));
        }
    }

    public List<? extends VarBind<?, ?, ?, ?>> getChildren() {
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

    public final <A> VarBind.Uno<A> bind1stage(String fieldname, UniValueNode.ValueType<A> type) {
        return bind1stage(fieldname, extractor(type));
    }

    public final <A> VarBind.Uno<A> bind1stage(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor
    ) {
        return new VarBind.Uno<>(this, fieldName, extractor);
    }

    private <A> BiFunction<UniObjectNode, String, A> extractor(final UniValueNode.ValueType<A> type) {
        return (root, fieldName) -> root.get(fieldName)
                .as(type);
    }

    public final <R> VarBind.Duo<UniObjectNode, R> bind2stage(
            String fieldName, Function<UniObjectNode, R> remapper
    ) {
        return bind2stage(fieldName, objectNodeExtractor, remapper);
    }

    public final <A, R> VarBind.Duo<A, R> bind2stage(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor, Function<A, R> remapper
    ) {
        return new VarBind.Duo<>(this, fieldName, extractor, remapper);
    }

    public final <A, R> VarBind.Duo<A, R> bind2stage(
            String fieldName, UniValueNode.ValueType<A> type, Function<A, R> remapper
    ) {
        return bind2stage(fieldName, extractor(type), remapper);
    }

    public final <R> VarBind.Dep<UniObjectNode, D, R> bindDependent(
            String fieldName, BiFunction<D, UniObjectNode, R> resolver
    ) {
        return bindDependent(fieldName, objectNodeExtractor, resolver);
    }

    public final <A, R> VarBind.Dep<A, D, R> bindDependent(
            String fieldName, BiFunction<UniObjectNode, String, A> extractor, BiFunction<D, A, R> resolver
    ) {
        return new VarBind.Dep<>(this, fieldName, extractor, resolver);
    }

    public final <A, R> VarBind.Dep<A, D, R> bindDependent(
            String fieldName, UniValueNode.ValueType<A> type, BiFunction<D, A, R> resolver
    ) {
        return bindDependent(fieldName, extractor(type), resolver);
    }

    public final <A, C extends Collection<A>> ArrayBind.Uno<A, C> list1stage(
            String fieldName, UniValueNode.ValueType<A> type, Supplier<C> collectionSupplier
    ) {
        return list1stage(fieldName, eachExtractor(type), collectionSupplier);
    }

    public final <A, C extends Collection<A>> ArrayBind.Uno<A, C> list1stage(
            String fieldName, Function<? extends UniNode, A> extractor, Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.Uno<>(this, fieldName, extractor, collectionSupplier);
    }

    private <A> Function<UniNode, A> eachExtractor(final UniValueNode.ValueType<A> type) {
        return root -> root.as(type);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.Duo<A, R, C> list2stage(
            String fieldName, UniValueNode.ValueType<A> type, Function<A, R> remapper, Supplier<C> collectionSupplier
    ) {
        return list2stage(fieldName, eachExtractor(type), remapper, collectionSupplier);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.Duo<A, R, C> list2stage(
            String fieldName, Function<? extends UniNode, A> extractor, Function<A, R> remapper, Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.Duo<>(this, fieldName, extractor, remapper, collectionSupplier);
    }

    public final <R, C extends Collection<R>> ArrayBind.Duo<UniObjectNode, R, C> list2stage(
            String fieldName, Function<UniObjectNode, R> remapper, Supplier<C> collectionSupplier
    ) {
        return list2stage(fieldName, UniNode::asObjectNode, remapper, collectionSupplier);
    }

    public final <R, C extends Collection<R>> ArrayBind.Dep<UniObjectNode, D, R, C> listDependent(
            String fieldName, BiFunction<D, UniObjectNode, R> resolver, Supplier<C> collectionSupplier
    ) {
        return listDependent(fieldName, UniNode::asObjectNode, resolver, collectionSupplier);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.Dep<A, D, R, C> listDependent(
            String fieldName,
            Function<? extends UniNode, A> extractor,
            BiFunction<D, A, R> resolver,
            Supplier<C> collectionSupplier
    ) {
        return new ArrayBind.Dep<>(this, fieldName, extractor, resolver, collectionSupplier);
    }

    public final <A, R, C extends Collection<R>> ArrayBind.Dep<A, D, R, C> listDependent(
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

    private static final BiFunction<UniObjectNode, String, UniObjectNode> objectNodeExtractor = (node, sub) -> node.get(sub)
            .asObjectNode();
}
