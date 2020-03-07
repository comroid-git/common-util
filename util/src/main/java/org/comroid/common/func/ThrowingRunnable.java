package org.comroid.common.func;

public interface ThrowingRunnable<R, T extends Throwable> {
    R run() throws T;
}
