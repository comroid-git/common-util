package org.comroid.common.iter.span;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.AbstractCollection;
import org.comroid.common.iter.ReferenceIndex;
import org.comroid.common.iter.pipe.Pipe;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

public class BasicSpan<T> implements AbstractCollection<T>, Span<T> {
    static final Span<?> EMPTY = new BasicSpan<>(ReferenceIndex.empty(), ModifyPolicy.IMMUTABLE);
    private final ReferenceIndex<T> storage;
    private final int fixedCapacity;
    private final ModifyPolicy modifyPolicy;
    private final Object dataLock = Polyfill.selfawareLock();

    @Override
    public final boolean isSingle() {
        return size() == 1;
    }

    @Override
    public final boolean isNotSingle() {
        return size() != 1;
    }

    @Override
    public final boolean isFixedSize() {
        return fixedCapacity != SIZE_UNFIXED;
    }

    public BasicSpan() {
        this(ReferenceIndex.create(), SIZE_UNFIXED, DEFAULT_MODIFY_POLICY);
    }

    public BasicSpan(int fixedCapacity) {
        this(ReferenceIndex.create(), fixedCapacity, DEFAULT_MODIFY_POLICY);
    }

    public BasicSpan(ReferenceIndex<T> referenceIndex, ModifyPolicy modifyPolicy) {
        this(referenceIndex, SIZE_UNFIXED, modifyPolicy);
    }

    protected BasicSpan(ReferenceIndex<? extends T> data, int fixedCapacity, ModifyPolicy modifyPolicy) {
        //noinspection unchecked
        this.storage = (ReferenceIndex<T>) data;
        this.fixedCapacity = fixedCapacity;
        this.modifyPolicy = modifyPolicy;
    }

    @Override
    @Contract
    public final int size() {
        return storage.size();
    }

    @NotNull
    @Override
    public final Iterator iterator() {
        synchronized (dataLock) {
            return new Iterator();
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("NullableProblems") // false positive
    public final Object[] toArray() {
        return toArray(new Object[0], Function.identity());
    }

    @Override
    @SuppressWarnings("NullableProblems") // false positive
    public final <R> @NotNull R[] toArray(@NotNull R[] dummy) {
        //noinspection unchecked
        return toArray(dummy, it -> (R) it);
    }

    @Override
    public final synchronized boolean add(T it) {
        synchronized (dataLock) {
            int i;

            for (i = 0; i < storage.size(); i++) {
                final T oldV = valueAt(i);
                if (modifyPolicy.canOverwrite(oldV, it)) {
                    storage.remove(oldV);
                    storage.add(it);
                    return true;
                }
            }

            if (isFixedSize()) {
                throw new IndexOutOfBoundsException("Span cannot be resized");
            }
            if (i != storage.size()) {
                throw new AssertionError(String.format(
                        "Suspicious Span.add() call: index too large {expected: %d, actual: %d}%n",
                        storage.size(), i
                ));
            }

            return modifyPolicy.canInitialize(it);
        }
    }

    @Override
    public final synchronized boolean remove(Object other) {
        synchronized (dataLock) {
            if (isFixedSize())
                throw new UnsupportedOperationException("Span has fixed size; cannot remove");

            for (int i = 0; i < storage.size(); i++) {
                final T valueAt = valueAt(i);

                if (valueAt == null)
                    throw new AssertionError();

                if (valueAt.equals(other)) {
                    if (!modifyPolicy.canRemove(other))
                        modifyPolicy.fail("removing " + other);

                    return storage.remove(valueAt);
                }
            }

            return false;
        }
    }

    @Override
    public final void clear() {
        synchronized (dataLock) {
            storage.clear();
        }
    }

    @Override
    public Pipe<?, T> pipe() {
        return null;
    }

    @Override
    public Reference<T> getReference(int index) {
        return null;
    }

    private <R> R[] toArray(R[] dummy, Function<Object, R> castOP) {
        synchronized (dataLock) {
            final R[] yields = Arrays.copyOf(dummy, size());

            for (int i, c = i = 0; i < storage.size(); i++) {
                final T valueAt = valueAt(i);

                if (modifyPolicy.canIterate(valueAt)) {
                    yields[c++] = castOP.apply(valueAt);
                }
            }

            return yields;
        }
    }

    private @Nullable T valueAt(int index) {
        synchronized (dataLock) {
            if (index >= size()) {
                return null;
            }

            T cast = storage.get(index);
            return modifyPolicy.canIterate(cast) ? cast : null;
        }
    }

    @Override
    public final String toString() {
        @NotNull Object[] arr = toArray();
        return String.format("Span{nullPolicy=%s, data={%d}%s}", modifyPolicy, arr.length, Arrays.toString(arr));
    }

    @Nullable
    @Override
    public final T get() {
        synchronized (dataLock) {
            for (int i = 0; i < storage.size(); i++) {
                final T valueAt = valueAt(i);

                if (modifyPolicy.canIterate(valueAt)) {
                    return valueAt;
                }
            }

            return null;
        }
    }

    @Override
    public @NotNull T requireNonNull() throws NullPointerException {
        return requireNonNull("No iterable value present");
    }

    @Override
    public Span<T> range(int startIncl, int endExcl) {
        synchronized (dataLock) {
            return new BasicSpan<>(storage.subset(startIncl, endExcl), ModifyPolicy.IMMUTABLE);
        }
    }

    @Override
    @Contract(mutates = "this")
    public void sort(Comparator<T> comparator) {
        synchronized (dataLock) {
            final List<T> list = new ArrayList<>(storage.unwrap());
            list.sort(comparator);

            storage.clear();
            list.forEach(storage::add);
        }

        cleanup();
    }

    @Override
    @Contract(mutates = "this")
    public final synchronized void cleanup() {
        synchronized (dataLock) {
            if (isFixedSize()) {
                final ArrayList<T> list = new ArrayList<>(storage.unwrap());

                storage.clear();
                list.stream()
                        .filter(it -> !modifyPolicy.canCleanup(it))
                        .forEachOrdered(storage::add);
            } else {
                final @NotNull Object[] array = toArray();

                storage.clear();
                for (Object it : array) //noinspection unchecked
                    storage.add((T) it);
            }
        }
    }

    @Override
    @Contract("-> new")
    public final API<T> reconfigure(/* todo: boolean parameter finalizeOldSpan? */) {
        return new API<>(this);
    }

    @Override
    public final <C extends Collection<T>> C into(Supplier<C> collectionSupplier) {
        final C coll = collectionSupplier.get();
        coll.addAll(this);

        return coll;
    }

    public final class Iterator implements java.util.Iterator<T> {
        private final Object[] dataSnapshot = toArray();
        private int previousIndex = -1;
        private @Nullable Object next;

        @Override
        public boolean hasNext() {
            return next != null || tryAcquireNext();
        }

        @Override
        public T next() {
            if (next != null || tryAcquireNext()) {
                //noinspection unchecked
                final T it = (T) next;
                next = null;

                return it;
            }

            throw new NoSuchElementException("Span had no more iterable values!");
        }

        private boolean tryAcquireNext() {
            int nextIndex = previousIndex + 1;
            if (next != null || nextIndex >= dataSnapshot.length) {
                return false;
            }

            next = dataSnapshot[nextIndex];

            while (!modifyPolicy.canIterate(next)) {
                tryAcquireNext();
            }

            previousIndex = nextIndex;
            return next != null;
        }
    }
}
