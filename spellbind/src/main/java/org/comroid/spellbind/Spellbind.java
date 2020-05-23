package org.comroid.spellbind;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.map.TrieMap;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.spellbind.annotation.Partial;
import org.comroid.spellbind.model.TypeFragment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.comroid.spellbind.SpellCore.methodString;

public final class Spellbind {
    private Spellbind() {
        throw new UnsupportedOperationException("Cannot instantiate " + Spellbind.class.getName());
    }

    public static <T extends TypeFragment<? super T>> Builder<T> builder(Class<T> mainInterface) {
        return new Builder<>(mainInterface);
    }

    static final class ReproxyFragment<S extends TypeFragment<? super S>> implements TypeFragment<S> {
        final CompletableFuture<S> future = new CompletableFuture<>();

        @Override
        public S self() {
            return future.join();
        }
    }

    public static class Builder<T extends TypeFragment<? super T>> implements org.comroid.common.func.Builder<T> {
        private final ReproxyFragment<T> reproxy = new ReproxyFragment<>();
        private final boolean internal;
        private final Class<T> mainInterface;
        private final Map<String, Invocable<Object>> methodBinds;
        private final Collection<Class<?>> interfaces;
        private Object coreObject;
        private ClassLoader classLoader;

        private Builder(Class<T> mainInterface) {
            this(false, mainInterface);
        }

        private Builder(boolean internal, Class<T> mainInterface) {
            this.internal = internal;
            this.mainInterface = mainInterface;
            this.methodBinds = TrieMap.ofString();
            this.interfaces = new ArrayList<>(1);

            interfaces.add(mainInterface);
        }

        static @Nullable Method findMatchingMethod(Method abstractMethod, Class<?> inClass) {
            for (Method method : inClass.getMethods()) {
                if (matchFootprint(abstractMethod, method)) {
                    return method;
                }
            }

            // TODO: 02.02.2020 doesnt work

            return null;
        }

        private static boolean matchFootprint(Method abstractMethod, Method method) {
            return abstractMethod.getName()
                    .equals(method.getName()) && Arrays.equals(abstractMethod.getParameterTypes(), method.getParameterTypes()) &&
                    abstractMethod.getReturnType()
                            .equals(method.getReturnType());
        }

        @Override
        public T build() {
            final ClassLoader classLoader = Optional.ofNullable(this.classLoader)
                    .orElseGet(Spellbind.class::getClassLoader);

            final SpellCore<T> spellCore = new SpellCore<>(reproxy, methodBinds);

            //noinspection unchecked
            final T it = (T) Proxy.newProxyInstance(classLoader,
                    interfaces.stream().distinct().toArray(Class[]::new), spellCore);
            reproxy.future.complete(it);

            return it;
        }

        public Builder<T> coreObject(@SuppressWarnings("rawtypes") TypeFragment coreObject) {
            this.coreObject = coreObject;

            final Class<?> coreObjectClass = coreObject.getClass();

            populateBinds(mainInterface.getMethods(), deepWrap(coreObject), methodBinds);

            return this;
        }

        private <X extends TypeFragment<? super X>> X deepWrap(final X it) {
            if (internal)
                return it;

            if (internal) return it;

            //noinspection unchecked
            return (X) new Builder<>(true, mainInterface)
                    .coreObject(it)
                    .subImplement(reproxy, Polyfill.uncheckedCast(SelfDeclared.class))
                    .build();
        }

        private void populateBinds(
                Method[] methods,
                @SuppressWarnings("rawtypes") TypeFragment implementationSource,
                Map<String, Invocable<Object>> map
        ) {
            for (Method method : methods) {
                final int mod = method.getModifiers();

                Method implMethod;
                if ((
                        implMethod = findMatchingMethod(method, implementationSource.getClass())
                ) != null) {
                    map.put(methodString(method), Invocable.ofMethodCall(implMethod, deepWrap(implementationSource)));
                }
            }
        }

        public Builder<T> subImplement(@SuppressWarnings("rawtypes") TypeFragment sub) {
            return Partial.Support.findPartialClass(sub.getClass())
                    .map(partialInterface -> subImplement(sub, Polyfill.uncheckedCast(partialInterface)))
                    .orElseThrow(() -> new IllegalArgumentException(String
                            .format("Class %s implements no @Partial annotated class", sub.getClass().getName())));
        }

        @SuppressWarnings("rawtypes")
        public Builder<T> subImplement(TypeFragment sub, Class<? extends TypeFragment> asInterface) {
            if (!Modifier.isInterface(asInterface.getModifiers())) {
                throw new IllegalArgumentException(String.format("Class %s is not an interface!", asInterface.getName()));
            }

            final Class<?> subClass = sub.getClass();
            Stream.of(subClass.getMethods())
                    .filter(method -> method.getDeclaringClass()
                            .equals(subClass))
                    .forEach(method -> Stream.of(asInterface.getMethods())
                            .filter(other -> matchFootprint(other, method))
                            .findAny()
                            .ifPresent(value -> methodBinds.put(methodString(value), Invocable.ofMethodCall(method, sub))));

            interfaces.add(asInterface);

            return this;
        }

        public Builder<T> classloader(ClassLoader classLoader) {
            this.classLoader = classLoader;

            return this;
        }
    }
}