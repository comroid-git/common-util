package org.comroid.common.spellbind.factory;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.map.TrieFuncMap;
import org.comroid.common.ref.Pair;
import org.comroid.common.spellbind.model.Invocable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class InstanceFactory<T, C extends InstanceContext<C>> extends ParamFactory.Abstract<C, T> {
    private final Map<Class[], Invocable> strategies;

    private InstanceFactory(Map<Class[], Invocable> strategies) {
        this.strategies = strategies;
    }

    @Override
    public T create(@Nullable C parameter) {
        final Class[] types = Arrays.stream(parameter.getArgs())
                .map(Object::getClass)
                .toArray(Class[]::new);
        final Invocable invocable = strategies.get(types);

        if (invocable == null)
            throw new UnsupportedOperationException(String.format("Cannot construct from types %s",
                    Arrays.toString(types)
            ));

        try {
            return (T) invocable.invoke(parameter.getArgs());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Builder<T, C extends InstanceContext<C>>
            implements org.comroid.common.func.Builder<InstanceFactory<T, C>> {
        private ParamFactory<Object[], Object> coreObjectFactory;
        private List<ImplementationNotation>   implementations = new ArrayList<>();
        private ClassLoader                    classLoader;

        public Builder(Class<T> mainInterface) {
        }

        public Builder<T, C> coreObject(ParamFactory<Object[], Object> coreObjectFactory) {
            this.coreObjectFactory = coreObjectFactory;
            return this;
        }

        public Builder<T, C> subImplement(ParamFactory<Object[], Object> subFactory, Class<?> asInterface) {
            this.implementations.add(new ImplementationNotation(subFactory, asInterface));
            return this;
        }

        public Builder<T, C> classloader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        @Override
        public InstanceFactory<T, C> build() {
            final Map<Class[], Invocable> strategies
                    = new TrieFuncMap<>((BiFunction<Class, Class, Boolean>) Class::isAssignableFrom,
                    Function.identity()
            );

            return new InstanceFactory<>(strategies);
        }
    }

    private static final class OrderedParamInstanceFactory<T> extends ParamFactory.Abstract<Object[], T> {
        private final Invocable invocable;

        private OrderedParamInstanceFactory(Invocable invocable) {
            this.invocable = invocable;
        }

        public Class[] getTypeOrder() {
            return invocable.typeOrder();
        }

        @Override
        public T create(@Nullable Object[] parameter) {
            try {
                return (T) invocable.invoke(parameter);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class ImplementationNotation extends Pair<ParamFactory<Object[], Object>, Class<?>> {
        public ImplementationNotation(ParamFactory<Object[], Object> first, Class<?> second) {
            super(first, second);
        }

        public ParamFactory<Object[], Object> getFactory() {
            return super.getFirst();
        }

        public Class<?> getType() {
            return super.getSecond();
        }
    }
}
