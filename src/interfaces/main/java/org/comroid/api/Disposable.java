package org.comroid.api;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Disposable extends AutoCloseable {
    Collection<? extends AutoCloseable> getChildren();

    void addChildren(AutoCloseable child);

    @OverrideOnly
    @SuppressWarnings("RedundantThrows")
    default void closeContainer() throws Exception {
    }

    @Override
    default void close() throws MultipleExceptions {
        disposeThrow();
    }

    default List<? extends Throwable> dispose() {
        return Collections.unmodifiableList(Stream.concat(
                Stream.<AutoCloseable>of(this::closeContainer),
                getChildren().stream()
        )
                .map(closeable -> {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        return e;
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    default void disposeThrow() throws MultipleExceptions {
        final List<? extends Throwable> throwables = dispose();

        if (throwables.isEmpty()) {
            return;
        }

        throw new MultipleExceptions(throwables);
    }

    interface Container extends Disposable {
        Disposable getUnderlyingDisposable();

        @Override
        default Collection<? extends AutoCloseable> getChildren() {
            return getUnderlyingDisposable().getChildren();
        }

        @Override
        default void addChildren(AutoCloseable child) {
            getUnderlyingDisposable().addChildren(child);
        }
    }

    class Basic implements Disposable {
        private final List<AutoCloseable> children = new ArrayList<>();

        @Override
        public List<AutoCloseable> getChildren() {
            return children;
        }

        @Override
        public void addChildren(AutoCloseable child) {
            if (child == this) {
                throw new IllegalArgumentException("Disposable cannot contain itself!");
            }

            children.add(child);
        }
    }

    final class MultipleExceptions extends RuntimeException {
        public MultipleExceptions(String message, Collection<? extends Throwable> causes) {
            super(composeMessage(message, causes));
        }

        public MultipleExceptions(Collection<? extends Throwable> causes) {
            super(composeMessage(null, causes));
        }

        private static String composeMessage(
                @Nullable String baseMessage, Collection<? extends Throwable> throwables
        ) {
            class StringStream extends OutputStream {
                private final StringBuilder sb = new StringBuilder();

                @Override
                public void write(int b) {
                    sb.append((char) b);
                }

                @Override
                public String toString() {
                    return sb.toString();
                }
            }

            if (baseMessage == null) {
                baseMessage = "Multiple Exceptions were thrown";
            }
            final StringStream out = new StringStream();
            final PrintStream string = new PrintStream(out);

            string.println(baseMessage);
            string.println("Sub Stacktraces in order:");
            throwables.forEach(t -> t.printStackTrace(string));

            return out.toString();
        }
    }
}
