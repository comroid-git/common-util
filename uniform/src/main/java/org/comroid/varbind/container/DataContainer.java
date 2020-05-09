package org.comroid.varbind.container;

import java.util.Optional;
import java.util.Set;

import org.comroid.common.func.Processor;
import org.comroid.common.ref.Reference;
import org.comroid.uniform.node.UniObjectNode;

import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataContainer<DEP> {
    GroupBind<?, DEP> getRootBind();

    Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node);

    Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet();

    <T> Optional<Reference<T>> getByName(String name);

    default <T> @Nullable T get(VarBind<?, ? super DEP, ?, T> bind) {
        return ref(bind).get();
    }

    <T> @NotNull Reference<T> ref(VarBind<?, ? super DEP, ?, T> bind);

    default <T> @NotNull Optional<T> wrap(VarBind<?, ? super DEP, ?, T> bind) {
        return ref(bind).wrap();
    }

    default @NotNull <T> T requireNonNull(VarBind<?, ? super DEP, ?, T> bind) {
        return ref(bind).requireNonNull();
    }

    default @NotNull <T> T requireNonNull(VarBind<?, ? super DEP, ?, T> bind, String message) {
        return ref(bind).requireNonNull(message);
    }

    default @NotNull <T> Processor<T> process(VarBind<?, ? super DEP, ?, T> bind) {
        return ref(bind).process();
    }

    DEP getDependencyObject();

    UniObjectNode toObjectNode(); // todo

    Class<? extends DataContainer<? super DEP>> getRepresentedType();

    interface Underlying<DEP> extends DataContainer<DEP> {
        DataContainer<DEP> getUnderlyingVarCarrier();

        @Override
        default GroupBind<?, DEP> getRootBind() {
            return getUnderlyingVarCarrier().getRootBind();
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
        @NotNull
        default <T> Reference<T> ref(VarBind<?, ? super DEP, ?, T> bind) {
            return getUnderlyingVarCarrier().ref(bind);
        }

        @Override
        default DEP getDependencyObject() {
            return getUnderlyingVarCarrier().getDependencyObject();
        }

        @Override
        default UniObjectNode toObjectNode() {
            return getUnderlyingVarCarrier().toObjectNode();
        }

        @Override
        default Class<? extends DataContainer<? super DEP>> getRepresentedType() {
            return getUnderlyingVarCarrier().getRepresentedType();
        }
    }
}
