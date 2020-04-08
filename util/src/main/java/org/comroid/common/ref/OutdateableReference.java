package org.comroid.common.ref;

import org.jetbrains.annotations.Nullable;

public final class OutdateableReference<T> implements Reference<T> {
    public boolean isOutdated() {
        synchronized (lock) {
            return outdated;
        }
    }

    private final Object  lock = new Object() {
        private volatile Object selfaware_keepalive = OutdateableReference.this.lock;
    };
    private       boolean outdated;
    private       T       it;

    @Override
    public @Nullable T get() {
        synchronized (lock) {
            return it;
        }
    }

    public void update(T newValue) {
        synchronized (lock) {
            this.it = newValue;
            outdated = false;
        }
    }

    public void outdate() {
        synchronized (lock) {
            outdated = true;
        }
    }
}