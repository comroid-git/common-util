package org.comroid.mutatio.ref;

import org.comroid.api.Polyfill;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class OutdateableReference<T> implements Reference<T> {
    private final Object lock = Polyfill.selfawareLock();
    private boolean outdated = true;
    private T it;

    public boolean isOutdated() {
        synchronized (lock) {
            return outdated || it != null;
        }
    }

    @Override
    public @Nullable T get() {
        synchronized (lock) {
            return it;
        }
    }

    /**
     * @return The new Value
     */
    public T update(T newValue) {
        synchronized (lock) {
            this.it = newValue;
            outdated = false;
            return newValue;
        }
    }

    /**
     * @return Whether the reference became outdated with this call.
     */
    public boolean outdate() {
        if (isOutdated()) {
            return false;
        }

        synchronized (lock) {
            return (outdated = true);
        }
    }

    public T compute(Supplier<T> supplier) {
        if (isOutdated())
            return update(supplier.get());
        else return get();
    }

    public static class SettableOfSupplier<T> extends OutdateableReference<T> implements Settable<T> {
        private final Settable<T> settable = Settable.create();
        private Supplier<T> supplier;

        public SettableOfSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
            update(null);
        }

        @Override
        public @Nullable T get() {
            if (!isOutdated())
                return supplier.get();
            return settable.get();
        }

        @Nullable
        @Override
        public T set(T newValue) {
            if (!isOutdated()) {
                outdate();
                settable.set(newValue);
                return supplier.get();
            }

            return settable.set(newValue);
        }

        @Override
        public boolean outdate() {
            final boolean outdate = super.outdate();
            if (outdate) supplier = null;
            return outdate;
        }
    }
}
