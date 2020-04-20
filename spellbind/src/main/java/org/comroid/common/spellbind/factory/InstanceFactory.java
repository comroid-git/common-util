package org.comroid.common.spellbind.factory;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.func.Provider;
import org.comroid.common.map.TrieFuncMap;
import org.comroid.common.ref.Pair;
import org.comroid.common.spellbind.model.Invocable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InstanceFactory<T, C extends InstanceContext<C>> extends ParamFactory.Abstract<C, T> {
    private final ClassLoader classLoader;
    private final Map<Class[], Invocable<T>> strategies;

    private InstanceFactory(ClassLoader classLoader, Map<Class[], Invocable<T>> strategies) {
        this.classLoader = classLoader;
        this.strategies  = Collections.unmodifiableMap(strategies);
    }

    public Set<Class[]> getParameterOrders() {
        return strategies.keySet();
    }

    @Override
    public T create(@Nullable C context) {
        final Class[] types = Arrays.stream(context.getArgs())
                .map(Object::getClass)
                .toArray(Class[]::new);
        final Invocable<T> invocable = strategies.get(types);

        if (invocable == null)
            throw new UnsupportedOperationException(String.format("Cannot construct from types %s",
                    Arrays.toString(types)
            ));

        try {
            return invocable.invoke(context.getArgs());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Builder<T, C extends InstanceContext<C>>
            implements org.comroid.common.func.Builder<InstanceFactory<T, C>> {
        private final Class<T>                        mainInterface;
        private       Invocable<?>                    coreObjectFactory;
        private       List<ImplementationNotation<?>> implementations = new ArrayList<>();
        private       ClassLoader                     classLoader;

        public Builder(Class<T> mainInterface) {
            this.mainInterface = mainInterface;
        }

        public <S extends T> Builder<T, C> coreObject(Provider<S> coreObjectFactory) {
            return coreObject(Invocable.ofProvider(coreObjectFactory));
        }

        public <S extends T> Builder<T, C> coreObject(Invocable<S> coreObjectFactory) {
            this.coreObjectFactory = coreObjectFactory;
            return this;
        }

        public <S extends T> Builder<T, C> subImplement(Provider<S> subFactory, Class<? super S> asInterface) {
            this.implementations.add(new ImplementationNotation(Invocable.ofProvider(subFactory), asInterface));
            return this;
        }

        public <S extends T> Builder<T, C> subImplement(Invocable<S> subFactory, Class<? super S> asInterface) {
            this.implementations.add(new ImplementationNotation(subFactory, asInterface));
            return this;
        }

        public Builder<T, C> classloader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        @Override
        public InstanceFactory<T, C> build() {
            final Map<Class[], Invocable<T>> strategies
                    = new TrieFuncMap<>((BiFunction<Class, Class, Boolean>) Class::isAssignableFrom,
                    Function.identity()
            );

            populateStrategies(strategies);

            this.get()

            return new InstanceFactory<>(classLoader, strategies);
        }

        private void populateStrategies(final Map<Class[], Invocable<T>> strategies) {
            final Invocable<?>[] allInvocations = allInvocations();
            final Class[] distinctTypes = Stream.of(allInvocations)
                    .map(Invocable::typeOrder)
                    .flatMap(Stream::of)
                    .distinct()
                    .toArray(Class[]::new);


        }

        private Invocable<?>[] allInvocations() {
            final List<Invocable<?>> collect = implementations.stream()
                    .map(ImplementationNotation::getFactory)
                    .collect(Collectors.toList());
            collect.add(coreObjectFactory);

            return collect.toArray(new Invocable[0]);
        }
    }

    @Internal
    private static final class ImplementationNotation<T> extends Pair<Invocable<T>, Class<? super T>> {
        public ImplementationNotation(Invocable<T> factory, Class<? super T> type) {
            super(factory, type);
        }

        public Invocable<T> getFactory() {
            return super.getFirst();
        }

        public Class<? super T> getType() {
            return super.getSecond();
        }
    }
}
