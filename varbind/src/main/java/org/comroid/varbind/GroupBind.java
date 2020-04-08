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
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import static org.comroid.common.Polyfill.deadCast;

public final class GroupBind {
    private final List<? extends VarBind<?, ?, ?, ?>> children = new ArrayList<>();
    private final SeriLib<?, ?, ?> seriLib;
    private final String groupName;

    public GroupBind(SeriLib<?, ?, ?> seriLib, String groupName) {
        this.seriLib = seriLib;
        this.groupName = groupName;
    }

    public <R extends VariableCarrier<D>, D> BiFunction<D, UniObjectNode, R> autoConstructor(
            Class<R> resultType, Class<D> dependencyType
    ) {
        final Class<?>[] typesUnordered = {
                SeriLib.class, seriLib.objectType.typeClass(), dependencyType
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
                        seriLib, obj, dependencyObject
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

    public final String getName() {
        return groupName;
    }

    public final List<? extends VarBind<?, ?, ?, ?>> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
