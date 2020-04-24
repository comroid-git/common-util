package org.comroid.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;

import org.comroid.common.func.ThrowingRunnable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.isNull;

public final class Polyfill {
    public static String regexGroupOrDefault(
            Matcher matcher, String groupName, @Nullable String orDefault
    ) {
        String cont;

        if (matcher.matches() && (cont = matcher.group(groupName)) != null) {
            return cont;
        } else if (orDefault != null) {
            return orDefault;
        } else {
            throw new NullPointerException("Group cannot be matched!");
        }
    }

    public static <R, T extends Throwable> Function<T, R> exceptionLogger() {
        return nil -> {
            nil.printStackTrace(System.err);

            return null;
        };
    }

    public static <T extends Throwable> URL url(
            String spec
    ) throws T {
        return url(spec, null);
    }

    public static <T extends Throwable> URL url(
            String spec, @Nullable Function<MalformedURLException, T> throwableReconfigurator
    ) throws T {
        if (throwableReconfigurator == null) {
            //noinspection unchecked
            throwableReconfigurator = cause -> (T) new AssertionError(cause);
        }

        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw throwableReconfigurator.apply(e);
        }
    }

    public static <T extends Throwable> URI uri(
            String spec
    ) throws T {
        return uri(spec, null);
    }

    public static <T extends Throwable> URI uri(
            String spec, Function<URISyntaxException, T> throwableReconfigurator
    ) throws T {
        if (throwableReconfigurator == null) {
            throwableReconfigurator = cause -> (T) new AssertionError(cause);
        }

        try {
            return new URI(spec);
        } catch (URISyntaxException e) {
            throw throwableReconfigurator.apply(e);
        }
    }

    public static <R, T extends Throwable> Runnable handlingRunnable(
            ThrowingRunnable<R, T> throwingRunnable, @Nullable Function<T, ? extends RuntimeException> remapper
    ) {
        final Function<T, ? extends RuntimeException> finalRemapper = notnullOr(remapper,
                (Function<T, ? extends RuntimeException>) RuntimeException::new
        );

        return () -> {
            try {
                throwingRunnable.run();
            } catch (Throwable thr) {
                throw finalRemapper.apply((T) thr);
            }
        };
    }

    public static <T> T notnullOr(@Nullable T value, @NotNull T def) {
        if (isNull(value)) {
            return def;
        }

        return value;
    }

    public static <R> R deadCast(Object instance) {
        return (R) instance;
    }

    public static <T, R> Function<T, R> failingFunction(Supplier<? extends RuntimeException> exceptionSupplier) {
        return new Function<T, R>() {
            private final Supplier<? extends RuntimeException> supplier = exceptionSupplier;

            @Override
            public R apply(T t) {
                throw supplier.get();
            }
        };
    }

    public static <T, R> Function<T, R> erroringFunction(@Nullable String message) {
        return new Function<T, R>() {
            private final String msg = notnullOr(message, "Unexpected Call");

            @Override
            public R apply(T t) {
                throw new AssertionError(msg);
            }
        };
    }

    public static Object selfawareLock() {
        class Lock {
            @Override
            public String toString() {
                return String.format("SelfAwareLock@%s", Integer.toHexString(hashCode()));
            }

            private volatile Object selfaware_keepalive;
        }

        final Lock lock = new Lock();
        lock.selfaware_keepalive = lock;

        return lock;
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    public static <T> Supplier<T> constantSupplier(T it) {
        return new Supplier<T>() {
            private final T value = it;

            @Override
            public T get() {
                return value;
            }
        };
    }
}
