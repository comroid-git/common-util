package org.comroid.varbind;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.uniform.data.SeriLib;
import org.comroid.varbind.bind.VarBind;

import org.jetbrains.annotations.NotNull;

import static java.util.function.Function.identity;
import static org.comroid.common.Polyfill.deadCast;

public final class VarBindFactory {
    public static @NotNull <T, BAS, TAR extends BAS> VarBind.Uno<T> simple(
            SeriLib<BAS, TAR, ?> seriLib,
            String fieldName,
            BiFunction<TAR, String, T> extractor
    ) {
        return new VarBind.Uno<>(null, fieldName, deadCast(extractor), identity());
    }

    public static @NotNull <T, C, BAS, TAR extends BAS> VarBind.Duo<T, C> mapped(
            SeriLib<BAS, TAR, ?> seriLib,
            String fieldName,
            BiFunction<TAR, String, C> extractor,
            Function<C, T> mapper) {
        return new VarBind.Duo<>(null, fieldName, deadCast(extractor), mapper);
    }

    public static @NotNull <T, C, D, BAS, TAR extends BAS> VarBind.Dep<T, C, D> dependent(
            SeriLib<BAS, TAR, ?> seriLib,
            String fieldName,
            BiFunction<TAR, String, C> extractor,
            BiFunction<D, C, T> resolver
    ) {
        return new VarBind.Dep<>(null, fieldName, deadCast(extractor), resolver);
    }
}
