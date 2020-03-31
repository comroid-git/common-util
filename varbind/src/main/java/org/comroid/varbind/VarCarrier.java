package org.comroid.varbind;

import java.util.Optional;
import java.util.Set;

import org.comroid.uniform.data.SeriLib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VarCarrier<BAS, OBJ extends BAS, DEP> {
    SeriLib<BAS, OBJ, ? extends BAS> getSerializationLibrary();

    GroupBind<BAS, OBJ, ?> getBindings();

    Set<VarBind<?, ?, ?, ?, OBJ>> updateFrom(OBJ node);

    Set<VarBind<?, ?, ?, ?, OBJ>> initiallySet();

    <T, A, R> @NotNull Optional<R> wrapVar(VarBind<T, A, ?, R, OBJ> bind);

    <T, A, R> @Nullable R getVar(VarBind<T, A, ?, R, OBJ> bind);
}
