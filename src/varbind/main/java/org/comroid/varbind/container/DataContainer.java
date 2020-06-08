package org.comroid.varbind.container;

import org.comroid.common.func.Processor;
import org.comroid.common.info.Dependent;
import org.comroid.mutatio.Span;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.ref.Reference;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface DataContainer<DEP> extends Dependent<DEP> {
    GroupBind<?, DEP> getRootBind();

    Class<? extends DataContainer<? super DEP>> getRepresentedType();

    Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node);

    Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet();

    <T> Optional<Reference<T>> getByName(String name);

    @Deprecated
    default <T> @NotNull Reference<T> ref(VarBind<?, ? super DEP, ?, T> bind) {
        return getComputedReference(bind);
    }

    default <T> @Nullable T get(VarBind<?, ? super DEP, ?, T> bind) {
        return getComputedReference(bind).get();
    }

    default <T> @NotNull Optional<T> wrap(VarBind<?, ? super DEP, ?, T> bind) {
        return getComputedReference(bind).wrap();
    }

    default @NotNull <T> T requireNonNull(VarBind<?, ? super DEP, ?, T> bind) {
        return getComputedReference(bind).requireNonNull();
    }

    default @NotNull <T> T requireNonNull(VarBind<?, ? super DEP, ?, T> bind, String message) {
        return getComputedReference(bind).requireNonNull(message);
    }

    default @NotNull <T> Processor<T> process(VarBind<?, ? super DEP, ?, T> bind) {
        return getComputedReference(bind).process();
    }

    default UniObjectNode toObjectNode(SerializationAdapter<?, ?, ?> serializationAdapter) {
        return toObjectNode(serializationAdapter.createUniObjectNode());
    }

    UniObjectNode toObjectNode(UniObjectNode node);

    default <T> @Nullable T put(VarBind<T, ? super DEP, ?, T> bind, T value) {
        return put(bind, Function.identity(), value);
    }

    <T, S> @Nullable T put(VarBind<S, ? super DEP, ?, T> bind, Function<T, S> parser, T value);

    <E> Reference.Settable<Span<E>> getExtractionReference(String fieldName);

    default <E> Reference.Settable<Span<E>> getExtractionReference(VarBind<E, ? super DEP, ?, ?> bind) {
        return getExtractionReference(cacheBind(bind));
    }

    <T, E> OutdateableReference<T> getComputedReference(VarBind<E, ? super DEP, ?, T> bind);

    @Internal
    <T> String cacheBind(VarBind<?, ? super DEP, ?, ?> bind);

    interface Underlying<DEP> extends DataContainer<DEP> {
        DataContainer<DEP> getUnderlyingVarCarrier();

        @Override
        default GroupBind<?, DEP> getRootBind() {
            return getUnderlyingVarCarrier().getRootBind();
        }

        @Override
        default DEP getDependent() {
            return getUnderlyingVarCarrier().getDependent();
        }

        @Override
        default Class<? extends DataContainer<? super DEP>> getRepresentedType() {
            return getUnderlyingVarCarrier().getRepresentedType();
        }

        @Override
        default Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node) {
            return getUnderlyingVarCarrier().updateFrom(node);
        }

        @Override
        default Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet() {
            return getUnderlyingVarCarrier().initiallySet();
        }

        @Override
        default <T> Optional<Reference<T>> getByName(String name) {
            return getUnderlyingVarCarrier().getByName(name);
        }

        @Override
        default <T, S> @Nullable T put(VarBind<S, ? super DEP, ?, T> bind, Function<T, S> parser, T value) {
            return getUnderlyingVarCarrier().put(bind, parser, value);
        }

        @Override
        default UniObjectNode toObjectNode(UniObjectNode node) {
            return getUnderlyingVarCarrier().toObjectNode(node);
        }

        @Override
        default <T> String cacheBind(VarBind<?, ? super DEP, ?, ?> bind) {
            return getUnderlyingVarCarrier().cacheBind(bind);
        }

        @Override
        default <E> Reference.Settable<Span<E>> getExtractionReference(String fieldName) {
            return getUnderlyingVarCarrier().getExtractionReference(fieldName);
        }

        @Override
        default <T, E> OutdateableReference<T> getComputedReference(VarBind<E, ? super DEP, ?, T> bind) {
            return getUnderlyingVarCarrier().getComputedReference(bind);
        }
    }
}
