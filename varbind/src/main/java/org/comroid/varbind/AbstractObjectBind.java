package org.comroid.varbind;

import java.util.Optional;
import java.util.function.BiFunction;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.Span;
import org.comroid.uniform.data.SeriLib;

import org.jetbrains.annotations.Nullable;

abstract class AbstractObjectBind<S, A, D, R, NODE> implements VarBind<S, A, D, R, NODE> {
    private final           Object                            seriLib;
    private final           String                            name;
    private final @Nullable GroupBind                         group;
    private final           BiFunction<NODE, String, Span<S>> extractor;

    protected AbstractObjectBind(
            Object seriLib,
            @Nullable GroupBind group,
            String name,
            BiFunction<NODE, String, Span<S>> extractor
    ) {
        this.seriLib   = seriLib;
        this.name      = name;
        this.group     = group;
        this.extractor = extractor;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public Span<S> extract(NODE NODE) {
        return extractor.apply(NODE, name);
    }

    @Override
    public String toString() {
        return String.format("VarBind@%s", getPath());
    }

    public final String getPath() {
        return getGroup().map(groupBind -> groupBind.getName() + ".")
                         .orElse("") + name;
    }

    @Override
    public final Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }

    protected <BAS, OBJ extends BAS, ARR extends BAS> SeriLib<BAS, OBJ, ARR> seriLib() {
        return Polyfill.deadCast(seriLib);
    }
}
