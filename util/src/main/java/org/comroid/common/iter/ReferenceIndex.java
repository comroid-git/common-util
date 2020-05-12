package org.comroid.common.iter;

import org.comroid.common.ref.Reference;
import org.comroid.common.iter.pipe.Pipe;
import org.comroid.common.iter.pipe.Pipeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ReferenceIndex<T> extends Pipeable<T> {
    static <T> ReferenceIndex<T> create() {
        return of(new ArrayList<>());
    }

    static <T> ReferenceIndex<T> of(List<T> list) {
        return new Support.OfList<>(list);
    }

    default List<T> unwrap() {
        final ArrayList<T> list = new ArrayList<>();
        pipe().forEach(list::add);
        return list;
    }

    default int size() {
        return unwrap().size();
    }

    boolean add(T item);

    boolean remove(T item);

    @Override
    Pipe<?, T> pipe();

    Reference<T> getReference(int index);

    @Nullable
    default T get(int index) {
        return getReference(index).get();
    }

    default Optional<T> wrap(int index) {
        return getReference(index).wrap();
    }

    default @NotNull T requireNonNull(int index) throws NullPointerException {
        return getReference(index).requireNonNull();
    }

    default @NotNull T requireNonNull(int index, String message) throws NullPointerException {
        return getReference(index).requireNonNull(message);
    }

    final class Support {
        private static final class OfList<T> implements ReferenceIndex<T> {
            private final List<T> underlying;

            private OfList(List<T> underlying) {
                this.underlying = underlying;
            }

            @Override
            public boolean add(T item) {
                return underlying.add(item);
            }

            @Override
            public boolean remove(T item) {
                return underlying.remove(item);
            }

            @Override
            public Pipe<?, T> pipe() {
                return Pipe.of(underlying);
            }

            @Override
            public Reference<T> getReference(int index) {
                return (index < 0 || underlying.size() >= index)
                        ? Reference.constant(underlying.get(index))
                        : Reference.empty();
            }
        }
    }
}
