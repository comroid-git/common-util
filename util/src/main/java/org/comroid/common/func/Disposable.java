package org.comroid.common.func;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.comroid.common.exception.MultipleExceptions;
import org.comroid.common.iter.Span;

public class Disposable implements Closeable {
    private final Span<Closeable> children = new Span<>();

    @Override
    public void close() {
        dispose();
    }

    public final List<? extends Throwable> dispose() {
        return children.stream()
                .map(closeable -> {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        return e;
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public final void disposeThrow() throws MultipleExceptions {
        final List<? extends Throwable> throwables = dispose();

        if (throwables.isEmpty()) {
            return;
        }

        throw new MultipleExceptions(throwables);
    }

    protected void disposeWith(Closeable child) {
        if (child == this) {
            throw new IllegalArgumentException("Disposable cannot contain itself!");
        }

        children.add(child);
    }
}
