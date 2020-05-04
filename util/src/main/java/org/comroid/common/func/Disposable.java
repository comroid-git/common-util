package org.comroid.common.func;

import org.comroid.common.exception.MultipleExceptions;
import org.comroid.common.iter.Span;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Disposable extends AutoCloseable {
    @Override
    default void close() throws MultipleExceptions {
        disposeThrow();
    }

    default List<? extends Throwable> dispose() {
        return Collections.unmodifiableList(getChildren().stream()
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

    void addChildren(AutoCloseable child);

    Collection<? extends AutoCloseable> getChildren();

    class Basic implements Disposable {
        private final Span<AutoCloseable> children = new Span<>();

        @Override
        public Span<AutoCloseable> getChildren() {
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

    interface Container extends Disposable {
        Disposable getUnderlyingDisposable();

        @Override
        default void addChildren(AutoCloseable child) {
            getUnderlyingDisposable().addChildren(child);
        }

        @Override
        default Collection<? extends AutoCloseable> getChildren() {
            return getUnderlyingDisposable().getChildren();
        }
    }
}
