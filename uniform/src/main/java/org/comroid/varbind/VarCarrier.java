package org.comroid.varbind;

import java.util.Optional;
import java.util.Set;

import org.comroid.uniform.node.UniObjectNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VarCarrier<DEP> {
    GroupBind getRootBind();

    Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node);

    Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet();

    <T> @Nullable T get(VarBind<?, ? super DEP, ?, T> bind);

    default <T> @NotNull Optional<T> wrap(VarBind<?, ? super DEP, ?, T> bind) {
        return Optional.ofNullable(get(bind));
    }

    default @NotNull <T> T requireNonNull(VarBind<?, ? super DEP, ?, T> bind) {
        return requireNonNull(bind, "No value defined");
    }

    default @NotNull <T> T requireNonNull(VarBind<?, ? super DEP, ?, T> bind, String message) {
        return wrap(bind).orElseThrow(() -> new NullPointerException(message));
    }
}
