package org.comroid.varbind.bind;

import java.util.Optional;

import org.comroid.common.func.bi.Junction;

import org.jetbrains.annotations.Nullable;

public final class VarBind<T, BAS, TAR extends BAS> implements GroupedBind {
    private final @Nullable GroupBind group;
    private final String name;
    private final Class<T> targetClass;
    private final T defaultValue;
    private final Junction<TAR, T> converter;

    private VarBind(@Nullable GroupBind group, String name, Class<T> targetClass, T defaultValue, Junction<TAR, T> converter) {
        this.group = group;
        this.name = name;
        this.targetClass = targetClass;
        this.defaultValue = defaultValue;
        this.converter = converter;
    }

    public T cast(Object inst) {
        if (!targetClass.isInstance(inst))
            throw new IllegalArgumentException(String.format("Invalid type: %s", inst.getClass().getName()));

        return targetClass.cast(inst);
    }

    @Override
    public Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }

    public T def() {
        return defaultValue;
    }

    public final String name() {
        return name;
    }

    public final Junction<TAR, T> converter() {
        return converter;
    }
}
