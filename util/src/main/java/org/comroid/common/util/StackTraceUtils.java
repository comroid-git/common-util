package org.comroid.common.util;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class StackTraceUtils {
    public static Class<?> callerClass(int skip) {
        return Stream.of(new Throwable().getStackTrace())
                .filter(ste -> ReflectionHelper.classExists(ste.getClassName()))
                .map(StackTraceElement::getClassName)
                .flatMap(className -> {
                    try {
                        return Stream.of(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        return Stream.empty();
                    }
                })
                .filter(it -> !it.isAnonymousClass() && !it.isSynthetic())
                .skip(1 + skip)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("Could not skip %d classes", skip)));
    }

    public static void putStackTrace(
            final List<String> lines, final Throwable throwable, final int omitAt, final boolean recursive
    ) {
        if (throwable == null) {
            return;
        }

        lines.add(String.format(
                "%s: %s",
                throwable.getClass().getName(),
                throwable.getMessage()
        ));

        final StackTraceElement[] stackTrace = throwable.getStackTrace();

        for (int c = 0; c < stackTrace.length && (omitAt == -1 || c < omitAt); c++) {
            lines.add(stackTrace[c].toString());
        }

        if (recursive) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                putStackTrace(lines, cause, omitAt, recursive);
            }
        }
    }
}
