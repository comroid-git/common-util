package org.comroid.varbind;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.comroid.uniform.data.SeriLib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VarCarrier<BAS, DEP> {
    GroupBind<BAS, ? extends BAS, ? extends BAS> getBindings();

    Set<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> updateFrom(BAS node);

    Set<VarBind<? extends BAS, Object, ? super DEP, ?, Object>> initiallySet();

    default <T> @NotNull Optional<T> wrap(VarBind<? extends BAS, Object, ? super DEP, ?, T> bind) {
        return Optional.ofNullable(get(bind));
    }

    <T> @Nullable T get(VarBind<? extends BAS, Object, ? super DEP, ?, T> bind);

    default @NotNull <T> T requireNonNull(VarBind<? extends BAS, Object, ? super DEP, ?, T> bind) {
        return Objects.requireNonNull(get(bind));
    }

    default @NotNull <T> T requireNonNull(
            VarBind<? extends BAS, Object, ? super DEP, ?, T> bind, String message
    ) {
        return Objects.requireNonNull(get(bind), message);
    }
}
