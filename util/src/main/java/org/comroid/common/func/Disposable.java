package org.comroid.common.func;

import org.comroid.common.exception.MultipleExceptions;
import org.comroid.common.iter.span.BasicSpan;
import org.comroid.common.iter.span.Span;
import org.comroid.common.ref.StaticCache;

import java.util.*;
import java.util.stream.Collectors;

public interface Disposable extends AutoCloseable {
    String CACHE_KEY = "disposable.children";

    default Collection<? extends AutoCloseable> getChildren() {
        return Collections.unmodifiableCollection(StaticCache.access(this, CACHE_KEY, ArrayList::new));
    }

    default void addChildren(AutoCloseable child) {
        StaticCache.access(this, CACHE_KEY, ArrayList::new).add(child);
    }

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
        private final Span<AutoCloseable> children = new BasicSpan<>();

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
}
