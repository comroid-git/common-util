package org.comroid.common.ref;

import org.comroid.common.Polyfill;

import org.jetbrains.annotations.Nullable;

public final class OutdateableReference<T> implements Reference<T> {
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
            this.it  = newValue;
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

    public boolean isOutdated() {
        synchronized (lock) {
            return outdated || it == null;
        }
    }
    private final Object  lock = Polyfill.selfawareLock();
    private       boolean outdated;
    private       T       it;
}
