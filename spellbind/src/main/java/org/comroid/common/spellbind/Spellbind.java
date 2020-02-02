package org.comroid.common.spellbind;

import java.lang.reflect.Proxy;
import java.util.Optional;

import org.comroid.common.spellbind.bound.SpellCore;

public final class Spellbind {
    private Spellbind() {
        throw new UnsupportedOperationException("Cannot instantiate " + Spellbind.class.getName());
    }

    public static <T> Builder<T> builder(Class<T> mainInterface) {
        return new Builder<>(mainInterface);
    }

    public static class Builder<T> {
        private final Class<T> mainInterface;
        private T coreObject;
        private ClassLoader classLoader;

        public Builder(Class<T> mainInterface) {
            this.mainInterface = mainInterface;
        }

        public <X extends T> Builder<T> coreObject(X coreObject) {
            this.coreObject = coreObject;

            return this;
        }

        public Builder<T> classloader(ClassLoader classLoader) {
            this.classLoader = classLoader;

            return this;
        }

        public T build() {
            final ClassLoader classLoader = Optional.ofNullable(this.classLoader).orElseGet(Spellbind.class::getClassLoader);
            final SpellCore spellCore = SpellCore.forCoreObject(mainInterface, coreObject);

            //noinspection unchecked
            return (T) Proxy.newProxyInstance(classLoader, new Class[]{mainInterface}, spellCore);
        }
    }
}