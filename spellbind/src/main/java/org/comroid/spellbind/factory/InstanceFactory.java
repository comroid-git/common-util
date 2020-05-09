package org.comroid.spellbind.factory;

import org.comroid.common.func.Invocable;
import org.comroid.common.info.Dependent;
import org.comroid.common.ref.Reference;
import org.comroid.spellbind.Spellbind;
import org.comroid.spellbind.model.SubImplementation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public final class InstanceFactory<T, D> implements Invocable.TypeMap<T>, Dependent<D> {
    private final Class<T> mainInterface;
    private final Reference<? extends T> coreObject;
    private final @Nullable D dependent;
    private final Collection<SubImplementation<?>> subImplementations;
    private final Class<?>[] requiredTypes;

    @Override
    public @Nullable D getDependent() {
        return dependent;
    }

    public InstanceFactory(Class<T> mainInterface, Reference<? extends T> coreObject, @Nullable D dependent, SubImplementation<?>... subImplementations) {
        this.mainInterface = mainInterface;
        this.coreObject = coreObject;
        this.dependent = dependent;
        this.subImplementations = Arrays.asList(subImplementations);
        this.requiredTypes = Stream.concat(
                dependent == null ? Stream.empty() : Stream.of(dependent.getClass()),
                this.subImplementations.stream()
                        .map(SubImplementation::getInstanceSupplier)
                        .map(Invocable::parameterTypesOrdered)
                        .flatMap(Arrays::stream)
        ).distinct().toArray(Class[]::new);
    }

    @Override
    public Class<?>[] parameterTypesOrdered() {
        return requiredTypes;
    }

    @Nullable
    @Override
    public T invoke(Map<Class<?>, Object> args) {
        final Spellbind.Builder<T> builder = Spellbind.builder(mainInterface);

        if (dependent != null)
            args.putIfAbsent(dependent.getClass(), dependent);

        builder.coreObject(coreObject.requireNonNull("Core Object"));
        subImplementations.forEach(sub -> builder
                .subImplement(
                        sub.getInstanceSupplier().autoInvoke(args.values().toArray()),
                        sub.getTargetInterface()
                ));

        return builder.build();
    }
}
