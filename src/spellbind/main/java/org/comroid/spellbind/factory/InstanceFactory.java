package org.comroid.spellbind.factory;

import org.comroid.api.Invocable;
import org.comroid.common.info.Dependent;
import org.comroid.common.ref.OutdateableReference;
import org.comroid.common.ref.Reference;
import org.comroid.spellbind.Spellbind;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public final class InstanceFactory<T, D> implements Invocable.TypeMap<T>, Dependent<D> {
    private final OutdateableReference<Class<?>[]> requiredTypes = new OutdateableReference<>();
    private final Class<T> mainInterface;
    private final Reference<? extends T> coreObject;
    private final @Nullable D dependent;
    private final Collection<TypeFragmentProvider<?>> typeFragmentProviders;

    @Override
    public @Nullable D getDependent() {
        return dependent;
    }

    public InstanceFactory(Class<T> mainInterface, Reference<? extends T> coreObject, @Nullable D dependent, TypeFragmentProvider<?>... typeFragmentProviders) {
        this.mainInterface = mainInterface;
        this.coreObject = coreObject;
        this.dependent = dependent;
        this.typeFragmentProviders = Arrays.asList(typeFragmentProviders);

        final Class<?>[] classes = parameterTypesOrdered();
        if (classes.length != streamParamTypes().count())
            throw new IllegalArgumentException("Duplicate parameter type detected: " + Arrays.toString(classes));
    }

    public void addSubimplementation(TypeFragmentProvider<?> fragmentProvider) {
        typeFragmentProviders.add(fragmentProvider);
        requiredTypes.outdate();
    }

    @Override
    public Class<?>[] parameterTypesOrdered() {
        return requiredTypes.compute(() -> streamParamTypes().distinct().toArray(Class[]::new));
    }

    @NotNull
    private Stream<Class<?>> streamParamTypes() {
        return Stream.concat(
                dependent == null ? Stream.empty() : Stream.of(dependent.getClass()),
                this.typeFragmentProviders.stream()
                        .map(TypeFragmentProvider::getInstanceSupplier)
                        .map(Invocable::parameterTypesOrdered)
                        .flatMap(Arrays::stream)
        );
    }

    @Nullable
    @Override
    public T invoke(Map<Class<?>, Object> args) {
        final Spellbind.Builder<T> builder = Spellbind.builder(mainInterface);

        if (dependent != null)
            args.putIfAbsent(dependent.getClass(), dependent);

        builder.coreObject(coreObject.requireNonNull("Core Object"));
        typeFragmentProviders.forEach(sub -> sub.accept(builder, args.values().toArray()));

        return builder.build();
    }
}
