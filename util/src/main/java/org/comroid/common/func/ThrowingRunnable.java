package org.comroid.common.func;

public interface ThrowingRunnable<T extends Throwable> {
    void run() throws T;
}
