package org.comroid.varbind;

import java.util.Optional;
import java.util.Set;

import org.comroid.common.func.Processor;
import org.comroid.common.ref.Reference;
import org.comroid.uniform.node.UniObjectNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VarCarrier<DEP> {
    GroupBind getRootBind();

    Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node);

    Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet();

    <T> Optional<Reference<T>> getByName(String name);

    <T> @NotNull Reference<T> ref(VarBind<?, ? super DEP, ?, T> bind);
  
    default <T> @Nullable T get(VarBind<?, ? super DEP, ?, T> bind) {
        return ref(bind).get();
    }

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

    interface Underlying<DEP> extends VarCarrier<DEP> {
        VarCarrier<DEP> getUnderlyingVarCarrier();

        @Override
        default GroupBind getRootBind() {
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
    }
}
