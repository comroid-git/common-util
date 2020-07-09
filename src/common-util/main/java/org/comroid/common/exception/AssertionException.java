package org.comroid.common.exception;

import org.jetbrains.annotations.Contract;

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

    private AssertionException(Object expected, Object actual, String detail) {
        this(String.format("Invalid data: expected %s%s, found %s", detail.isEmpty() ? "" : (detail + " == "), expected, actual));
    }

    public static <X, Y> boolean expect(X expected, Y actual) throws AssertionException {
        return expect(expected, actual, "");
    }

    public static <X, Y> boolean expect(X expected, Y actual, String detail) throws AssertionException {
        return expect(expected, actual, Objects::equals, detail);
    }

    public static <X, Y> boolean expect(X expected, Y actual, BiPredicate<X, Y> condition) throws AssertionException {
        return expect(expected, actual, condition, "");
    }

    public static <X, Y> boolean expect(X expected, Y actual, BiPredicate<X, Y> condition, String detail) throws AssertionException {
        if (condition.test(expected, actual))
            throw new AssertionException(expected, actual, detail);

        return true;
    }
}
