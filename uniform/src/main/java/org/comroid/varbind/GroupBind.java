package org.comroid.varbind;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.SerializationAdapter;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;
import org.comroid.uniform.data.node.UniValueNode;

public final class GroupBind {
    private final List<? extends VarBind<?, ?, ?, ?>> children = new ArrayList<>();
    private final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final String groupName;

    public GroupBind(SerializationAdapter<?, ?, ?> serializationAdapter, String groupName) {
        this.serializationAdapter = serializationAdapter;
        this.groupName = groupName;
    }

    public <R extends VariableCarrier<D>, D> BiFunction<D, UniObjectNode, R> autoConstructor(
            Class<R> resultType, Class<D> dependencyType
    ) {
        final Class<?>[] typesUnordered = {
                SerializationAdapter.class, serializationAdapter.objectType.typeClass(), dependencyType
        };
        final Optional<Constructor<R>> optConstructor = ReflectionHelper.findConstructor(resultType,
                typesUnordered
        );

        if (!optConstructor.isPresent()) throw new NoSuchElementException(
                "Could not find any fitting constructor");

        class Resolver implements BiFunction<D, UniObjectNode, R> {
            private final Constructor<R> constr;

            public Resolver(Constructor<R> constr) {
                this.constr = constr;
            }

            @Override
            public R apply(D dependencyObject, UniObjectNode obj) {
                return ReflectionHelper.instance(constr, ReflectionHelper.arrange(new Object[]{
                        serializationAdapter, obj, dependencyObject
                }, constr.getParameterTypes()));
            }
        }

        return new Resolver(optConstructor.get());
    }

    private <T> BiFunction<UniObjectNode, String, T> extractor(final Class<T> extractTarget) {
        return (node, fieldName) -> extractTarget.cast(node.get(fieldName).asRaw(null));
    }

    private <T> BiFunction<UniArrayNode, String, Collection<T>> splitExtractor(
            BiFunction<UniObjectNode, String, T> dataExtractor) {
        return (arrayNode, fieldName) -> arrayNode.asNodeList()
                .stream()
                .map(UniNode::asObjectNode)
                .filter(node -> !node.isNull())
                .map(node -> dataExtractor.apply(node, fieldName))
                .collect(Collectors.toList());
    }

    private <T> BiFunction<UniObjectNode, String, T> extractor(final UniValueNode.ValueType<T> type) {
        return (root, fieldName) -> root.get(fieldName).as(type);
    }

    private <T> Function<UniNode, T> eachExtractor(final UniValueNode.ValueType<T> type) {
        return root -> root.as(type);
    }

    public final String getName() {
        return groupName;
    }

    public final List<? extends VarBind<?, ?, ?, ?>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public final <T> VarBind.Uno<T> bind1stage(String fieldname, UniValueNode.ValueType<T> type) {
        return bind1stage(fieldname, extractor(type));
    }

    public final <T> VarBind.Uno<T> bind1stage(String fieldName, BiFunction<UniObjectNode, String, T> extractor) {
        return new VarBind.Uno<>(this, fieldName, extractor);
    }

    public final <T, R> VarBind.Duo<T, R> bind2stage(String fieldName, UniValueNode.ValueType<T> type, Function<T, R> remapper) {
        return bind2stage(fieldName, extractor(type), remapper);
    }

    public final <T, R> VarBind.Duo<T, R> bind2stage(String fieldName, BiFunction<UniObjectNode, String, T> extractor, Function<T, R> remapper) {
        return new VarBind.Duo<>(this, fieldName, extractor, remapper);
    }

    public final <T, D, R> VarBind.Dep<T, D, R> bindDependent(String fieldName, UniValueNode.ValueType<T> type, BiFunction<T, D, R> resolver) {
        return bindDependent(fieldName, extractor(type), resolver);
    }

    public final <T, D, R> VarBind.Dep<T, D, R> bindDependent(String fieldName, BiFunction<UniObjectNode, String, T> extractor, BiFunction<T, D, R> resolver) {
        return new VarBind.Dep<>(this, fieldName, extractor, resolver);
    }

    public final <T, C extends Collection<T>> ArrayBind.Uno<T, C> list1stage(String fieldName, UniValueNode.ValueType<T> type, Supplier<C> collectionSupplier) {
        return list1stage(fieldName, eachExtractor(type), collectionSupplier);
    }

    public final <T, C extends Collection<T>> ArrayBind.Uno<T, C> list1stage(String fieldName, Function<? extends UniNode, T> extractor, Supplier<C> collectionSupplier) {
        return new ArrayBind.Uno<>(this, fieldName, extractor, collectionSupplier);
    }

    public final <T, R, C extends Collection<R>> ArrayBind.Duo<T, R, C> list2stage(String fieldName, UniValueNode.ValueType<T> type, Function<T, R> remapper, Supplier<C> collectionSupplier) {
        return list2stage(fieldName, eachExtractor(type), remapper, collectionSupplier);
    }

    public final <T, R, C extends Collection<R>> ArrayBind.Duo<T, R, C> list2stage(String fieldName, Function<? extends UniNode, T> extractor, Function<T, R> remapper, Supplier<C> collectionSupplier) {
        return new ArrayBind.Duo<>(this, fieldName, extractor, remapper, collectionSupplier);
    }

    public final <T, D, R, C extends Collection<R>> ArrayBind.Dep<T, D, R, C> listDependent(String fieldName, UniValueNode.ValueType<T> type, BiFunction<T, D, R> resolver, Supplier<C> collectionSupplier) {
        return listDependent(fieldName, eachExtractor(type), resolver, collectionSupplier);
    }

    public final <T, D, R, C extends Collection<R>> ArrayBind.Dep<T, D, R, C> listDependent(String fieldName, Function<? extends UniNode, T> extractor, BiFunction<T, D, R> resolver, Supplier<C> collectionSupplier) {
        return new ArrayBind.Dep<>(this, fieldName, extractor, resolver, collectionSupplier);
    }
}
