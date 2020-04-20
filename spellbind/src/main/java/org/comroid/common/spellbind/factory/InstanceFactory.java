package org.comroid.common.spellbind.factory;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.map.TrieMap;
import org.comroid.common.spellbind.model.Invocable;
import org.comroid.common.util.ReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InstanceFactory<C extends InstanceContext<C>, T> extends ParamFactory.Abstract<C, T> {
    private final Map<String, Invocable> strategies = TrieMap.ofString();

    @Override
    public T create(@Nullable C parameter) {
        final String argsFootprint = Stream.of(parameter.getArgs())
                .map(Object::getClass)
                .map(ReflectionHelper::canonicalClass)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", ", "(", ")"));

        return null;
    }
}
