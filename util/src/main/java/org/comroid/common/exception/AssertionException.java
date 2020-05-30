package org.comroid.common.exception;

import java.util.Objects;
import java.util.function.BiPredicate;

public class AssertionException extends RuntimeException {
    public AssertionException() {
    }

    public AssertionException(String message) {
        super(message);
    }

    public AssertionException(Throwable cause) {
        super(cause);
    }

    public AssertionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssertionException(Object expected, Object actual) {
        this(String.format("Invalid data: expected %s, found %s", expected, actual));
    }

    public static <X, Y> boolean expect(X expected, Y actual) throws AssertionException {
        return expect(expected, actual, Objects::equals);
    }

    public static <X, Y> boolean expect(X expected, Y actual, BiPredicate<X, Y> condition) throws AssertionException {
        if (condition.test(expected, actual))
            throw new AssertionException(expected, actual);

        return true;
    }
}
