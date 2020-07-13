package org.comroid.mutatio.cache;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface CachedValue<T> {
    /**
     * @return Whether {@link #outdate()} was called on this container and it hasn't been {@linkplain #update(Object) updated} yet.
     */
    boolean isOutdated();

    default boolean isUpToDate() {
        return !isOutdated();
    }

    /**
     * <p>Implementation Note: The value should already be stored when this method is called.</p>
     *
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
        private final AtomicBoolean outdated = new AtomicBoolean(false);

        @Override
        public boolean isOutdated() {
            return outdated.get();
        }

        @Override
        public T update(T withValue) {
            outdated.set(false);
            listeners.forEach(listener -> listener.acceptNewValue(withValue));
            return withValue;
        }

        @Override
        public boolean outdate() {
            if (isOutdated())
                return false;
            outdated.set(true);
            return true;
        }

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
