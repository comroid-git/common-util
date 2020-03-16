package org.comroid.varbind;

import java.util.Optional;
import java.util.function.BiFunction;

import org.comroid.common.iter.Span;

import org.jetbrains.annotations.Nullable;

abstract class AbstractObjectBind<S, A, D, R, NODE> implements VarBind<S, A, D, R, NODE> {
    private final String name;
    private final @Nullable GroupBind group;
    private final BiFunction<NODE, String, Span<S>> extractor;

    protected AbstractObjectBind(@Nullable GroupBind group, String name, BiFunction<NODE, String, Span<S>> extractor) {
        this.name = name;
        this.group = group;
        this.extractor = extractor;
    }

    @Override
    public final Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }

    @Override
    public final Span<S> extract(NODE node) {
        return extractor.apply(node, name);
    }

    @Override
    public final String getName() {
        return name;
    }
}
