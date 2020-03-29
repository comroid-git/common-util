package org.comroid.common.iter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.max;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Span<T> implements AbstractCollection<T>, Supplier<Optional<T>> {
    public static final int        DEFAULT_INITIAL_CAPACITY = 0;
    public static final NullPolicy DEFAULT_NULL_POLICY      = NullPolicy.DEFAULT;
    public static final boolean    DEFAULT_FIXED_SIZE       = false;

    public static <T> Span.API<T> make() {
        return new Span.API<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> Span<T> zeroSize() {
        return (Span<T>) ZeroSize;
    }

    private static final Span<?>          ZeroSize      = new Span<>(new Object[0],
                                                                     NullPolicy.IGNORE,
                                                                     true
    );
    private static final IntUnaryOperator enlargementOp = x -> x + 1;

    //region API Class
    public static final class API<T> {
        private Collection<T> initialValues = new ArrayList<>();
        private NullPolicy    nullPolicy    = DEFAULT_NULL_POLICY;
        private boolean       fixedSize     = DEFAULT_FIXED_SIZE;

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> initialSize(int initialSize) {
            this.initialValues = new ArrayList<>(initialSize);

            return this;
        }

        public Collector<T, ?, Span<T>> collector() {
            class SpanCollector implements Collector<T, Span<T>, Span<T>> {
                private final Supplier<Span<T>>          supplier    = Span::new;
                private final BiConsumer<Span<T>, T>     accumulator = Span::add;
                private final BinaryOperator<Span<T>>    combiner    = (ts, ts2) -> {
                    ts.addAll(ts2);

                    return ts;
                };
                private final Collection<T>              initialValues;
                private final NullPolicy                 nullPolicy;
                private final boolean                    fixedSize;
                private final Function<Span<T>, Span<T>> finisher    = new Function<Span<T>, Span<T>>() {
                    @Override
                    public Span<T> apply(Span<T> ts) {
                        return Span.<T>make().nullPolicy(nullPolicy)
                                             .fixedSize(fixedSize)
                                             .initialValues(initialValues)
                                             .initialValues(ts)
                                             .span();
                    }
                };

                public SpanCollector(
                        Collection<T> initialValues, NullPolicy nullPolicy, boolean fixedSize
                ) {
                    this.initialValues = initialValues;
                    this.nullPolicy    = nullPolicy;
                    this.fixedSize     = fixedSize;
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

            return new SpanCollector(initialValues, nullPolicy, fixedSize);
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> initialValues(Collection<T> values) {
            initialValues.addAll(values);

            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> nullPolicy(NullPolicy nullPolicy) {
            this.nullPolicy = nullPolicy;

            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> fixedSize(boolean fixedSize) {
            this.fixedSize = fixedSize;

            return this;
        }

        public Span<T> span() {
            return new Span<>(initialValues.toArray(), nullPolicy, fixedSize);
        }

        @SafeVarargs
        @Contract(value = "_ -> this", mutates = "this")
        public final API<T> initialValues(T... values) {
            return initialValues(Arrays.asList(values));
        }
    }
    //endregion

    public final boolean isSingle() {
        return size() == 1;
    }

    @Override
    @Contract
    public final int size() {
        synchronized (dataLock) {
            int c = data.length;

            for (Object each : data) {
                if (!nullPolicy.canIterate(each)) c--;
            }

            return c;
        }
    }

    @Override
    public final @NotNull SpanIterator iterator() {
        synchronized (dataLock) {
            return new SpanIterator();
        }
    }

    @Override
    public final @NotNull Object[] toArray() {
        return toArray(new Object[0]);
    }

    @Override
    @Contract
    public final @NotNull <A> A[] toArray(@NotNull A[] dummy) {
        synchronized (dataLock) {
            @NotNull A[] yield    = Arrays.copyOf(dummy, size());
            Iterator<T>  iterator = iterator();

            int i = 0;
            while (iterator.hasNext() && i < yield.length) {
                yield[i++] = (A) iterator.next();
            }

            return yield;
        }
    }

    @Override
    @Contract(mutates = "this")
    public final boolean add(T item) {
        synchronized (dataLock) {
            for (
                    int i = 0; i >= data.length && !fixedSize
                    ? i < adjustArray(i, false)
                    : i < data.length; i++
            ) {
                if (nullPolicy.canOverwrite(valueAt(i), item)) {
                    replace(i, item, true);
                    last = i;
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    @Contract(mutates = "this")
    public final boolean remove(Object item) {
        synchronized (dataLock) {
            return remove(item, true) > -1;
        }
    }

    @Override
    @Contract(mutates = "this")
    public final void clear() {
        synchronized (dataLock) {
            if (fixedSize) {
                IntStream.range(0, data.length)
                         .forEach(i -> replace(i, null, true));
            } else {
                data = Arrays.copyOf(data, 0);
                last = 0;
            }
        }
    }
    public final @Nullable T replace(int index, @Nullable T next, boolean force) {
        final int size = size();

        adjustArray(index, false);

        final T old = valueAt(index);

        if (!force && !nullPolicy.canOverwrite(old, next)) nullPolicy.fail(String.format("Cannot overwrite %s with %s",
                                                                                         old,
                                                                                         next
        ));

        synchronized (dataLock) {
            data[index] = next;
        }

        if (index >= last) last = index;

        return old;
    }

    public final int adjustArray(int lastIndex, boolean allowShrink) {
        if (fixedSize) throw new IndexOutOfBoundsException("Span cannot be resized");

        int diff = data.length;

        if (lastIndex > data.length - 1) {
            do {
                enlargeArray();
            }
            while (lastIndex > data.length - 1);

            diff = data.length - diff;
        } else if (lastIndex < data.length) {
            // todo: implement unary operator that works based of time and accesses
            if (allowShrink) trimArray(lastIndex);

            diff = diff - data.length;
        } else return 0;

        return diff;
    }

    private int enlargeArray() {
        synchronized (dataLock) {
            final int newLength = intLimit(enlargementOp.applyAsInt(data.length));

            data = Arrays.copyOf(data, newLength);

            return data.length;
        }
    }

    private int intLimit(long var) {
        if (var >= ((long) Integer.MAX_VALUE * (long) 2))
            throw new StackOverflowError(String.format("Span %s ran out of space", toString()));

        if (var >= Integer.MAX_VALUE) {
            System.err.printf("[WARNING] Span is running out of space: %s", toString());
            return Integer.MAX_VALUE;
        }

        return (int) var;
    }

    @Override
    public String toString() {
        @NotNull Object[] arr = toArray();
        return String.format("Span{nullPolicy=%s, data={%d}%s}",
                             nullPolicy,
                             arr.length,
                             Arrays.toString(arr)
        );
    }

    private int trimArray(int toSize) {
        synchronized (dataLock) {
            data = Arrays.copyOf(data, toSize + 1);
        }

        return data.length;
    }

    private @Nullable T valueAt(int index) {
        if (index >= size()) return null;

        return (T) data[index];
    }

    public final int remove(Object item, boolean force) {
        int removed = 0;

        for (int i = 0; i < size(); i++) {
            T each = valueAt(i);

            if (nullPolicy.canIterate(each) && item.equals(each)) {
                remove(i, force);
                removed++;
            }
        }

        return removed;
    }

    public final boolean remove(int index, boolean force) {
        if (index > data.length) return false;

        replace(index, null, force);
        return true;
    }

    public @NotNull T getAssert() throws AssertionError {
        return get(String.format("Could not get from %s: no iterable values present", toString()));
    }

    public @NotNull T get(final String exceptionMessage) throws AssertionError {
        return get(() -> new AssertionError(exceptionMessage));
    }

    public <E extends Throwable> @NotNull T get(Supplier<E> exceptionSupplier) throws E {
        return get().orElseThrow(exceptionSupplier);
    }

    @Override
    public @NotNull Optional<T> get() {
        final Object[] objects = stream().filter(nullPolicy::canIterate)
                                         .toArray();
        return objects.length >= 1 ? Optional.ofNullable((T) objects[0]) : Optional.empty();
    }

    private final NullPolicy nullPolicy;
    private final Object     dataLock = new Object() {
        private volatile Object selfaware_keepalive = Span.this.dataLock;
    };
    private       Object[]   data;
    private       int        last;
    private       boolean    fixedSize;

    public Span() {
        this(new Object[DEFAULT_INITIAL_CAPACITY], DEFAULT_NULL_POLICY, DEFAULT_FIXED_SIZE);
    }

    private Span(Object[] data, NullPolicy nullPolicy, boolean fixedSize) {
        this.nullPolicy = nullPolicy;

        this.data      = data;
        this.last      = data.length;
        this.fixedSize = fixedSize;
    }

    @SafeVarargs
    @Deprecated
    public Span(T... initialValues) {
        this(Arrays.asList(initialValues));
    }

    @Deprecated
    public Span(Collection<T> initialValues) {
        this(initialValues.size(), DEFAULT_NULL_POLICY, DEFAULT_FIXED_SIZE);

        addAll(initialValues);
    }

    @Deprecated
    public Span(int initialCapacity, NullPolicy nullPolicy, boolean fixedSize) {
        this(new Object[max(initialCapacity, 1)], nullPolicy, fixedSize);
    }

    @Contract("null -> fail; _ -> new")
    public Span<T> policy(NullPolicy nullPolicy) {
        return new Span<>(Arrays.copyOf(data, data.length),
                          Objects.requireNonNull(nullPolicy),
                          true
        );
    }

    @Contract(mutates = "this")
    public void sort(Comparator<T> comparator) {
        synchronized (dataLock) {
            data = stream().sorted(comparator)
                           .toArray();
        }

        cleanup();
    }

    @Contract(mutates = "this")
    public final synchronized void cleanup() {
        synchronized (dataLock) {
            data = Stream.of(data)
                         .filter(it -> !nullPolicy.canCleanup(it))
                         .toArray(Object[]::new);

            last = data.length;

            if (!fixedSize) trimArray(last);
        }
    }

    public int count(Object instance) {
        return (int) stream().filter(instance::equals)
                             .count();
    }

    @Contract(mutates = "this")
    public boolean appendAll(@NotNull Collection<? extends T> c) {
        return c.stream()
                .anyMatch(this::append);
    }

    @Contract(mutates = "this")
    public boolean append(T item) {
        if (!nullPolicy.canInitialize(item)) nullPolicy.fail(String.format("Cannot initialize %s @ index: %d; array length: %d",
                                                                           item,
                                                                           last + 1,
                                                                           data.length
        ));

        this.replace(++last, item, false);

        return true;
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum NullPolicy {
        //endformatting
        IGNORE(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> true,
                cleanup -> isNull(cleanup)
        ),

        DEFAULT(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> isNull(overwriting),
                cleanup -> isNull(cleanup)
        ),

        INITIALIZATION(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with),
                cleanup -> isNull(cleanup)
        ),

        SKIP(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> !isNull(overwriting) || nonNull(with),
                cleanup -> isNull(cleanup)
        ),

        OVERWRITE_ONLY(
                init -> nonNull(init),
                iterate -> nonNull(iterate),
                (overwriting, with) -> isNull(overwriting),
                cleanup -> isNull(cleanup)
        ),

        PROHIBIT(
                init -> Objects.requireNonNull(init, "NullPolicy PROHIBIT was violated on initializing") != null || false,
                iterate -> Objects.requireNonNull(iterate, "NullPolicy PROHIBIT was violated on iterating") != null || false,
                (overwriting, with) -> Objects.requireNonNull(overwriting, "NullPolicy PROHIBIT was violated on overwriting") != null || false,
                cleanup -> isNull(cleanup)
        );
        //startformatting

        private final static Object                      dummy = new Object();
        private final        Predicate<Object>           initVarTester;
        private final        Predicate<Object>           iterateVarTester;
        private final        BiPredicate<Object, Object> overwriteTester;
        private final        Predicate<Object>           cleanupTester;

        NullPolicy(
                Predicate<Object> initVarTester,
                Predicate<Object> iterateVarTester,
                BiPredicate<Object, Object> overwriteTester,
                Predicate<Object> cleanupTester
        ) {

            this.initVarTester    = initVarTester;
            this.iterateVarTester = iterateVarTester;
            this.overwriteTester  = overwriteTester;
            this.cleanupTester    = cleanupTester;
        }

        public boolean canInitialize(Object var) {
            return initVarTester.test(var);
        }

        public boolean canIterate(Object var) {
            return iterateVarTester.test(var);
        }

        public boolean canOverwrite(Object old, Object with) {
            return overwriteTester.test(old, with);
        }

        public boolean canCleanup(Object var) {
            return cleanupTester.test(var);
        }

        public void fail(String message) throws NullPointerException {
            throw new NullPointerException(String.format("NullPolicy %s was violated: %s",
                                                         name(),
                                                         message
            ));
        }
    }

    public final class SpanIterator implements Iterator<T> {
        private final Object[] data = Arrays.copyOf(Span.this.data, Span.this.data.length);
        private       int      c    = 0;

        @Override
        @Contract(pure = true)
        public final boolean hasNext() {
            if (c >= data.length) return false;

            int i = c;
            for (; i < data.length; i++) {
                if (nullPolicy.canIterate(data[i])) return true;
            }

            return false;
        }

        @Override
        public final T next() {
            int i = c;
            for (; i < data.length; i++) {
                if (nullPolicy.canIterate(data[i])) {
                    c = i + 1;
                    return (T) data[i];
                }
            }

            throw new NoSuchElementException("No more iterables in SpanIterator");
        }
    }
}
