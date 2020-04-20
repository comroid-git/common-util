package org.comroid.common.spellbind.factory;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.func.Provider;
import org.comroid.common.map.TrieFuncMap;
import org.comroid.common.ref.Pair;
import org.comroid.common.spellbind.Spellbind;
import org.comroid.common.func.Invocable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InstanceFactory<T> extends ParamFactory.Abstract<InstanceContext, T> {
    private final ClassLoader                classLoader;
    private final Map<Class[], Invocable<T>> strategies;

    private InstanceFactory(ClassLoader classLoader, Map<Class[], Invocable<T>> strategies) {
        this.classLoader = classLoader;
        this.strategies  = Collections.unmodifiableMap(strategies);
    }

    public Set<Class[]> getParameterOrders() {
        return strategies.keySet();
    }

    @Override
    public T create(@Nullable InstanceContext context) {
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
            implements org.comroid.common.func.Builder<InstanceFactory<T>> {
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
        public InstanceFactory<T> build() {
            final Map<Class[], Invocable<T>> strategies
                    = new TrieFuncMap<>((BiFunction<Class, Class, Boolean>) Class::isAssignableFrom,
                    Function.identity()
            );

            populateStrategies(strategies);

            return new InstanceFactory<>(classLoader, strategies);
        }

        private void populateStrategies(final Map<Class[], Invocable<T>> strategies) {
            final Class[] distinctTypes = Stream.of(allInvocations())
                    .map(Invocable::typeOrder)
                    .flatMap(Stream::of)
                    .distinct()
                    .toArray(Class[]::new);

            strategies.put(distinctTypes,
                    new CombiningInvocable<>(mainInterface,
                            distinctTypes,
                            classLoader,
                            coreObjectFactory,
                            implementations
                    )
            );
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
    private static final class CombiningInvocable<T> implements Invocable<T> {
        private final Class<T>                        mainInterface;
        private final Class[]                         types;
        private final ClassLoader                     classLoader;
        private final Invocable<?>                    coreObjectFactory;
        private final List<ImplementationNotation<?>> notations;

        private CombiningInvocable(
                Class<T> mainInterface,
                Class[] types,
                ClassLoader classLoader,
                Invocable<?> coreObjectFactory,
                List<ImplementationNotation<?>> notations
        ) {
            this.mainInterface     = mainInterface;
            this.types             = types;
            this.classLoader       = classLoader;
            this.coreObjectFactory = coreObjectFactory;
            this.notations         = notations;
        }

        @Nullable
        @Override
        public T invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
            final Spellbind.Builder<T> builder = Spellbind.builder(mainInterface)
                    .classloader(classLoader)
                    .coreObject(coreObjectFactory.invokeAutoOrder(args));

            for (ImplementationNotation<?> notation : notations) {
                builder.subImplement(notation.getFactory()
                        .invokeAutoOrder(args), notation.getType());
            }

            return builder.build();
        }

        @Override
        public Class[] typeOrder() {
            return types;
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
