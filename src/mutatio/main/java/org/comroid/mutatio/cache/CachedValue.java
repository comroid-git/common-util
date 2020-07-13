package org.comroid.mutatio.cache;

import org.comroid.mutatio.span.Span;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public interface CachedValue<T> {
    /**
     * @return Whether {@link #outdate()} was called on this container and it hasn't been {@linkplain #update(Object) updated} yet.
     */
    boolean isOutdated();

    /**
     * @return The new Value
     */
    T update(T withValue);

    /**
     * @return Whether the reference became outdated with this call.
     */
    boolean outdate();

    default ValueUpdateListener<T> onChange(Consumer<T> consumer) {
        return ValueUpdateListener.ofConsumer(this, consumer);
    }

    @Internal
    boolean attach(ValueUpdateListener<T> listener);

    @Internal
    boolean detach(ValueUpdateListener<T> listener);

    abstract class Abstract<T> implements CachedValue<T> {
        private final Set<ValueUpdateListener<T>> listeners = new HashSet<>();

        @Override
        public boolean attach(ValueUpdateListener<T> listener) {
            return listeners.add(listener);
        }

        @Override
        public boolean detach(ValueUpdateListener<T> listener) {
            return listeners.remove(listener);
        }
    }
}
