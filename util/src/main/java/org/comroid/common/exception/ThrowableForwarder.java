package org.comroid.common.exception;

import org.comroid.common.func.ThrowingRunnable;
import org.comroid.common.func.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class ThrowableForwarder<T extends Throwable, O extends RuntimeException> {
    private final Function<T, O> acceptor;

    public ThrowableForwarder(Function<T, O> acceptor) {
        this.acceptor = acceptor;
    }

    public void handle(T throwable) {
        final O result = acceptor.apply(throwable);

        if (result != null)
            throw result;
    }

    public <R> @NotNull R execute(ThrowingSupplier<? extends R, T> supplier) throws O {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            //noinspection unchecked
            handle((T) throwable);
        }

        throw new AssertionException("Could not handle throwable");
    }

    public <R> @NotNull R execute(ThrowingRunnable<? extends R, T> supplier) throws O {
        try {
            return supplier.run();
        } catch (Throwable throwable) {
            //noinspection unchecked
            handle((T) throwable);
        }

        throw new AssertionException("Could not handle throwable");
    }

    public static <T extends Throwable> ThrowableForwarder<T, RuntimeException> rethrowing() {
        return new ThrowableForwarder<>(RuntimeException::new);
    }
}
