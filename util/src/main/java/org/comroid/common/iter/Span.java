package org.comroid.common.iter;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.pipe.Pipe;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.Objects.nonNull;

public class Span<T> implements AbstractCollection<T>, ReferenceIndex<T>, Reference<T> {
    public static final boolean DEFAULT_FIXED_SIZE = false;
    public static final int DEFAULT_INITIAL_CAPACITY = 0;
    public static final ModifyPolicy DEFAULT_MODIFY_POLICY = ModifyPolicy.SKIP_NULLS;
    private static final Span<?> ZeroSize = new Span<>(new Object[0], ModifyPolicy.IMMUTABLE, true);
    private final ReferenceIndex<T> storage = ReferenceIndex.create();
    private final ModifyPolicy modifyPolicy;
    private final Object dataLock = Polyfill.selfawareLock();
    private final boolean fixedSize;

    public final boolean isSingle() {
        return size() == 1;
    }

    public final boolean isNotSingle() {
        return size() != 1;
    }

    public Span() {
        this(new Object[DEFAULT_INITIAL_CAPACITY], DEFAULT_MODIFY_POLICY, DEFAULT_FIXED_SIZE);
    }

    public Span(int capacity, boolean fixedSize) {
        this(new Object[capacity], DEFAULT_MODIFY_POLICY, fixedSize);
    }

    protected Span(Object[] data, ModifyPolicy modifyPolicy, boolean fixedSize) {
        this.modifyPolicy = modifyPolicy;
        this.fixedSize = fixedSize;
    }

    public static <T> Collector<T, ?, Span<T>> collector() {
        return Span.<T>make().fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .collector();
    }

    public static <T> Span.API<T> make() {
        return new Span.API<>();
    }

    public static <T> Span<T> zeroSize() {
        //noinspection unchecked
        return (Span<T>) ZeroSize;
    }

    public static <T> Span<T> singleton(T it) {
        return Span.<T>make().initialValues(it)
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
    }

    public static <T> Span<T> immutable(Collection<T> of) {
        return Span.<T>make().initialValues(of)
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
    }

    @SafeVarargs
    public static <T> Span<T> immutable(T... of) {
        return Span.<T>make().initialValues(of)
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
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
    public final Object[] toArray() {
        return toArray(new Object[0], Function.identity());
    }

    @NotNull
    @Override
    public final <R> R[] toArray(@NotNull R[] dummy) {
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

            if (fixedSize) {
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
            if (fixedSize)
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

    public Span<T> range(int startIncl, int endExcl) {
        synchronized (dataLock) {
            return new Span<>(Arrays.copyOfRange(toArray(), startIncl, endExcl), ModifyPolicy.IMMUTABLE, true);
        }
    }

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

    @Contract(mutates = "this")
    public final synchronized void cleanup() {
        synchronized (dataLock) {
            if (fixedSize) {
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

    @Contract("-> new")
    public final API<T> reconfigure(/* todo: boolean parameter finalizeOldSpan? */) {
        return new API<>(this);
    }

    public final <C extends Collection<T>> C into(Supplier<C> collectionSupplier) {
        final C coll = collectionSupplier.get();
        coll.addAll(this);

        return coll;
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum ModifyPolicy {
        //endformatting
        SKIP_NULLS(init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> Objects.isNull(overwriting),
                remove -> true,
                cleanup -> Objects.isNull(cleanup)
        ),

        NULL_ON_INIT(init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with) && Objects.isNull(overwriting),
                remove -> true,
                cleanup -> Objects.isNull(cleanup)
        ),

        PROHIBIT_NULLS(init -> {
            Objects.requireNonNull(init);
            return true;
        },
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with) && Objects.isNull(overwriting),
                remove -> true,
                cleanup -> Objects.isNull(cleanup)
        ),

        IMMUTABLE(init -> true, iterate -> true, (overwriting, with) -> false, remove -> false, cleanup -> false);
        //startformatting

        private final static Object dummy = new Object();
        private final Predicate<Object> initVarTester;
        private final Predicate<Object> iterateVarTester;
        private final BiPredicate<Object, Object> overwriteTester;
        private final Predicate<Object> removeTester;
        private final Predicate<Object> cleanupTester;

        ModifyPolicy(
                Predicate<Object> initVarTester,
                Predicate<Object> iterateVarTester,
                BiPredicate<Object, Object> overwriteTester,
                Predicate<Object> removeTester,
                Predicate<Object> cleanupTester
        ) {

            this.initVarTester = initVarTester;
            this.iterateVarTester = iterateVarTester;
            this.overwriteTester = overwriteTester;
            this.removeTester = removeTester;
            this.cleanupTester = cleanupTester;
        }

        public boolean canInitialize(Object var) {
            return var != dummy && initVarTester.test(var);
        }

        public boolean canIterate(Object var) {
            return var != dummy && iterateVarTester.test(var);
        }

        public boolean canOverwrite(Object old, Object with) {
            return (old != dummy && with != dummy) && overwriteTester.test(old, with);
        }

        public boolean canRemove(Object var) {
            return var != dummy && removeTester.test(var);
        }

        public boolean canCleanup(Object var) {
            return var != dummy && cleanupTester.test(var);
        }

        public void fail(String message) throws NullPointerException {
            throw new NullPointerException(String.format("NullPolicy %s was violated: %s", name(), message));
        }
    }

    //region API Class
    public static final class API<T> {
        private Collection<T> initialValues;
        private ModifyPolicy modifyPolicy;
        private boolean fixedSize;

        public API() {
            this.initialValues = new ArrayList<>();
            this.modifyPolicy = DEFAULT_MODIFY_POLICY;
            this.fixedSize = DEFAULT_FIXED_SIZE;
        }

        private API(Span<T> base) {
            initialValues = new ArrayList<>(base);
            modifyPolicy = base.modifyPolicy;
            fixedSize = base.fixedSize;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> initialSize(int initialSize) {
            this.initialValues = new ArrayList<>(initialSize);

            return this;
        }

        public Collector<T, ?, Span<T>> collector() {
            class SpanCollector implements Collector<T, Span<T>, Span<T>> {
                private final Supplier<Span<T>> supplier = Span::new;
                private final BiConsumer<Span<T>, T> accumulator = Span::add;
                private final BinaryOperator<Span<T>> combiner = (ts, ts2) -> {
                    ts.addAll(ts2);

                    return ts;
                };
                private final Collection<T> initialValues;
                private final ModifyPolicy nullPolicy;
                private final boolean fixedSize;
                private final Function<Span<T>, Span<T>> finisher = new Function<Span<T>, Span<T>>() {
                    @Override
                    public Span<T> apply(Span<T> ts) {
                        return Span.<T>make().modifyPolicy(nullPolicy)
                                .fixedSize(fixedSize)
                                .initialValues(initialValues)
                                .initialValues(ts)
                                .span();
                    }
                };

                public SpanCollector(
                        Collection<T> initialValues, ModifyPolicy nullPolicy, boolean fixedSize
                ) {
                    this.initialValues = initialValues;
                    this.nullPolicy = nullPolicy;
                    this.fixedSize = fixedSize;
                }

                @Override
                public Supplier<Span<T>> supplier() {
                    return supplier;
                }

                @Override
                public BiConsumer<Span<T>, T> accumulator() {
                    return accumulator;
                }

                @Override
                public BinaryOperator<Span<T>> combiner() {
                    return combiner;
                }

                @Override
                public Function<Span<T>, Span<T>> finisher() {
                    return finisher;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return Collections.singleton(Characteristics.IDENTITY_FINISH);
                }
            }

            return new SpanCollector(initialValues, modifyPolicy, fixedSize);
        }

        public Span<T> span() {
            return new Span<>(initialValues.toArray(), modifyPolicy, fixedSize);
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> initialValues(Collection<T> values) {
            initialValues.addAll(values);

            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> fixedSize(boolean fixedSize) {
            this.fixedSize = fixedSize;

            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> modifyPolicy(ModifyPolicy modifyPolicy) {
            this.modifyPolicy = modifyPolicy;

            return this;
        }

        @SafeVarargs
        @Contract(value = "_ -> this", mutates = "this")
        public final API<T> initialValues(T... values) {
            return initialValues(Arrays.asList(values));
        }
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
