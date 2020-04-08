package org.comroid.varbind;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.comroid.uniform.node.UniObjectNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VarCarrier<DEP> {
    GroupBind getRootBind();

    Set<VarBind<Object, ?, ?, Object>> updateFrom(UniObjectNode node);

    Set<VarBind<Object, ? super DEP, ?, Object>> initiallySet();

    default <T> @NotNull Optional<T> wrap(VarBind<Object, ? super DEP, ?, T> bind) {
        return Optional.ofNullable(get(bind));
    }

    <T> @Nullable T get(VarBind<Object, ? super DEP, ?, T> bind);

    default @NotNull <T> T requireNonNull(VarBind<Object, ? super DEP, ?, T> bind) {
        return Objects.requireNonNull(get(bind));
    }

    default @NotNull <T> T requireNonNull(
            VarBind<Object, ? super DEP, ?, T> bind, String message
    ) {
        return Objects.requireNonNull(get(bind), message);
    }
}
