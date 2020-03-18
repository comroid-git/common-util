package org.comroid.common.iter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Span<T> implements Collection<T>, Supplier<Optional<T>> {
    @Deprecated
    public static final int DEFAULT_INITIAL_CAPACITY = 1;
    @Deprecated
    public static final NullPolicy DEFAULT_NULL_POLICY = NullPolicy.IGNORE;
    @Deprecated
    public static final boolean DEFAULT_ALLOW_STRETCHING = true;
    private static final Span<?> ZeroSize = new Span<>(new Object[0], NullPolicy.IGNORE, false);

    @Deprecated
    public static <T> Collector<T, Collection<T>, Span<T>> collector(final boolean fixedSize) {
        return Collector.of(
                Span::new,
                Collection::add,
                (list, other) -> {
                    list.addAll(other);

                    return list;
                },
                ts -> fixedSize ? Span.fixedSize(ts) : new Span<>(ts),
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    @Deprecated
    public static <T> Collector<T, Collection<T>, Span<T>> collector(final Span<T> dummy) {
        return Collector.of(
                Span::new,
                Collection::add,
                (list, other) -> {
                    list.addAll(other);

                    return list;
                },
                ts -> {
                    final Span span = new Span<>(ts.size(), dummy.nullPolicy, dummy.stretch);
                    span.addAll(ts);
                    return span;
                },
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    @Deprecated
    public static <T> Span<T> fixedSize(Collection<T> contents) {
        final Span<T> span = new Span<>(contents.size(), NullPolicy.IGNORE, false);

        span.addAll(contents);

        return span;
    }

    @SafeVarargs
    @Deprecated
    public static <T> Span<T> fixedSize(T... contents) {
        return fixedSize(Arrays.asList(contents));
    }

    @Deprecated
    public static <T> Span<T> fixedSize$NotNull(Collection<T> contents) {
        return new Span<T>(contents.size(), NullPolicy.INITIALIZATION, false) {{
            addAll(contents);
        }};
    }

    @SafeVarargs
    @Deprecated
    public static <T> Span<T> fixedSize$NotNull(T... contents) {
        return fixedSize$NotNull(Arrays.asList(contents));
    }

    public static <T> Span.API<T> api() {
        return new Span.API<>();
    }

    public static <T> Span<T> zeroSize() {
        return (Span<T>) ZeroSize;
    }

    private final NullPolicy nullPolicy;
    private Object[] data;
    private int last;
    private boolean stretch;

    @Deprecated
    public Span() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_NULL_POLICY, DEFAULT_ALLOW_STRETCHING);
    }

    @SafeVarargs
    @Deprecated
    public Span(T... initialValues) {
        this(Arrays.asList(initialValues));
    }

    @Deprecated
    public Span(Collection<T> initialValues) {
        this(initialValues.size(), DEFAULT_NULL_POLICY, DEFAULT_ALLOW_STRETCHING);

        addAll(initialValues);
    }

    @Deprecated
    public Span(int initialCapacity, NullPolicy nullPolicy, boolean fixedSize) {
        this(new Object[Math.max(initialCapacity, 1)], nullPolicy, fixedSize);
    }

    private Span(Object[] data, NullPolicy nullPolicy, boolean fixedSize) {
        this.nullPolicy = nullPolicy;

        this.data = data;
        this.last = data.length;
        this.stretch = !fixedSize;
    }

    public boolean isSingle() {
        return size() == 1;
    }

    @Contract("null -> fail; _ -> new")
    public Span<T> policy(NullPolicy nullPolicy) {
        return new Span<>(Arrays.copyOf(data, data.length), Objects.requireNonNull(nullPolicy), true);
    }

    @Override
    @Contract
    public int size() {
        long count = 0L;

        for (Object t : data)
            if (nullPolicy.canIterate(t))
                count++;

        return (int) count;
    }

    @Override
    public boolean isEmpty() {
        return stream().noneMatch(nullPolicy::canIterate) || size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return stream().anyMatch(o::equals);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        class SpanIterator implements Iterator<T> {
            private int i = 0;

            @Override
            @Contract(pure = true)
            public boolean hasNext() {
                if (i >= size())
                    return false;

                // todo Test this
                long toSkip = i;
                for (Object it : data) {
                    if (toSkip > 0) {
                        toSkip--;
                        continue;
                    }

                    if (!nullPolicy.canIterate(it))
                        return true;
                }

                return false;
            }

            @Override
            public T next() {
                final T value = value(i++);

                if (!nullPolicy.canIterate(value))
                    return next();

                return value;
            }
        }

        return new SpanIterator();
    }

    @Override
    public @NotNull Object[] toArray() {
        return toArray(new Object[0]);
    }

    @Override
    public @NotNull <A> A[] toArray(@NotNull A[] dummy) {
        return stream()
                .limit(size())
                .map(it -> (A) it)
                .toArray(val -> Arrays.copyOf(dummy, val));
    }

    @Override
    @Contract(mutates = "this")
    public boolean add(T item) {
        int i = 0;

        while (!nullPolicy.canOverwrite(value(i), item)) {
            if (i >= data.length) {
                adjustArray(i);
                break;
            }
            i++;
        }

        set(i, item);
        return true;
    }

    @Override
    @Contract(mutates = "this")
    public boolean remove(Object item) {
        boolean yield = false;

        for (int i = 0; i < size(); i++) {
            final T it = value(i);

            if (item.equals(it)) {
                set(i, null);
                yield = true;
            }
        }

        System.out.printf("..%b\n", yield);

        return yield;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    @Contract(mutates = "this")
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean added = false;

        for (T item : c)
            if (add(item))
                added = true;

        return added;
    }

    @Override
    @Contract(mutates = "this")
    public boolean removeAll(@NotNull Collection<?> c) {
        return c.stream()
                .map(this::remove)
                .collect(Collectors.toList())
                .stream()
                .anyMatch(Boolean::booleanValue);
    }

    @Override
    @Contract(mutates = "this")
    public boolean retainAll(@NotNull Collection<?> c) {
        final List<T> collect = stream()
                .filter(item -> !contains(item))
                .collect(Collectors.toList());

        final boolean removeAll = removeAll(collect);
        cleanup();


        return removeAll;
    }

    @Override
    @Contract(mutates = "this")
    public void clear() {
        removeAll(this);
    }

    @Override
    @Contract(pure = true)
    public Stream<T> stream() {
        return Stream.of(data)
                .limit(size())
                .map(it -> (T) it);
    }

    @Contract(mutates = "this")
    public void cleanup() {
        data = Stream.of(data)
                .filter(it -> !nullPolicy.canCleanup(it))
                .toArray(Object[]::new);

        last = data.length;

        if (stretch) trimArray(last);
    }

    @Contract(mutates = "this")
    public boolean append(T item) {
        if (!nullPolicy.canInitialize(item))
            nullPolicy.fail();

        this.set(++last, item);

        return true;
    }

    @Contract(mutates = "this")
    public boolean appendAll(@NotNull Collection<? extends T> c) {
        return c.stream().anyMatch(this::append);
    }

    @Override
    public @NotNull Optional<T> get() {
        final Object[] objects = stream()
                .filter(nullPolicy::canIterate)
                .toArray();
        return objects.length >= 1 ? Optional.ofNullable((T) objects[0]) : Optional.empty();
    }

    public <E extends Throwable> @NotNull T get(Supplier<E> exceptionSupplier) throws E {
        return get().orElseThrow(exceptionSupplier);
    }

    public @NotNull T get(final String exceptionMessage) throws AssertionError {
        return get(() -> new AssertionError(exceptionMessage));
    }

    public @NotNull T getAssert() throws AssertionError {
        return get(String.format("Could not get from %s: no iterable values present", toString()));
    }

    @Override
    public String toString() {
        return String.format("Span{nullPolicy=%s, data=%s}", nullPolicy, Arrays.toString(data));
    }

    private @Nullable T set(int index, @Nullable T next) {
        final int size = size();

        if (index < 0 || (!stretch && index >= data.length))
            throw new IndexOutOfBoundsException("Span cannot be resized");
        if (stretch) adjustArray(index);

        final T old = value(index);

        if (!nullPolicy.canOverwrite(old, next))
            nullPolicy.fail();

        data[index] = next;

        if (index >= last)
            last = index;
        cleanup();

        return old;
    }

    private int adjustArray(int index) {
        if (data.length > index)
            return 0;

        int c = 0;
        while (enlargeArray() < index)
            c++;

        return c;
    }

    private int enlargeArray() {
        data = Arrays.copyOf(data, intLimit(Math.max(1, data.length * 2)));

        return data.length;
    }

    private int trimArray(int toSize) {
        data = Arrays.copyOf(data, toSize);

        return data.length;
    }

    private int intLimit(long var) {
        if (var >= ((long) Integer.MAX_VALUE * (long) 2))
            throw new StackOverflowError(String.format("Span %s ran out of space", toString()));

        if (var >= Integer.MAX_VALUE) {
            System.err.printf("WARNING: %s is running out of space!", toString());
            return Integer.MAX_VALUE;
        }

        return (int) var;
    }

    private @Nullable T value(int index) {
        if (index >= size())
            return null;

        return (T) data[index];
    }

    public static final class API<T> {
        private final Collection<T> initialValues = new ArrayList<>();
        private int initialSize = DEFAULT_INITIAL_CAPACITY;
        private NullPolicy nullPolicy = DEFAULT_NULL_POLICY;
        private boolean fixedSize = DEFAULT_ALLOW_STRETCHING;

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> initialSize(int initialSize) {
            this.initialSize = initialSize;

            return this;
        }

        @SafeVarargs
        @Contract(value = "_ -> this", mutates = "this")
        public final API<T> initialValues(T... values) {
            return initialValues(Arrays.asList(values));
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

        public Collector<T, ?, Span<T>> collector() {
            class SpanCollector implements Collector<T, Span<T>, Span<T>> {
                private final Supplier<Span<T>> supplier = Span::new;
                private final BiConsumer<Span<T>, T> accumulator = Span::add;
                private final BinaryOperator<Span<T>> combiner = (ts, ts2) -> {
                    ts.addAll(ts2);

                    return ts;
                };
                private final Collection<T> initialValues;
                private final NullPolicy nullPolicy;
                private final boolean fixedSize;
                private final Function<Span<T>, Span<T>> finisher = new Function<Span<T>, Span<T>>() {
                    @Override
                    public Span<T> apply(Span<T> ts) {
                        return Span.<T>api()
                                .nullPolicy(nullPolicy)
                                .fixedSize(fixedSize)
                                .initialValues(initialValues)
                                .initialValues(ts)
                                .span();
                    }
                };

                public SpanCollector(Collection<T> initialValues, NullPolicy nullPolicy, boolean fixedSize) {
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

            return new SpanCollector(initialValues, nullPolicy, fixedSize);
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum NullPolicy {
        /**
         * Nulls are ignored.
         */
        IGNORE(
                init -> true,
                iterate -> true,
                (overwriting, with) -> true,
                cleanup -> false
        ),

        /**
         * Nulls are only allowed in initializing actions
         */
        INITIALIZATION(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with),
                cleanup -> isNull(cleanup)
        ),

        /**
         * Nulls are removed on {@link Span#cleanup()}.
         */
        CLEANUP_NULLS(
                init -> true,
                iterate -> true,
                (overwriting, with) -> true,
                cleanup -> isNull(cleanup)
        ),

        /**
         * Skip nulls in iteration, but don't prohibit any actions.
         */
        SKIP_ON_ITERATE(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> true,
                cleanup -> isNull(cleanup)
        ),

        /**
         * Skip nulls on iterating actions and prohibit overwriting nonnulls with null.
         */
        SKIP(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> !isNull(overwriting) || nonNull(with),
                cleanup -> isNull(cleanup)
        ),

        /**
         * Only overwrite nulls, don't allow nulls on initialization or appending.
         */
        OVERWRITE_ONLY(
                init -> nonNull(init),
                iterate -> nonNull(iterate),
                (overwriting, with) -> true,
                cleanup -> isNull(cleanup)
        ),

        /**
         * Prohibit nulls in any case.
         */
        PROHIBIT(
                init -> Objects.requireNonNull(init, "NullPolicy PROHIBIT was violated on initializing") != null || false,
                iterate -> Objects.requireNonNull(iterate, "NullPolicy PROHIBIT was violated on iterating") != null || false,
                (overwriting, with) -> Objects.requireNonNull(overwriting, "NullPolicy PROHIBIT was violated on overwriting") != null || false,
                cleanup -> isNull(cleanup)
        );

        private final static Object dummy = new Object();
        private final Predicate<Object> initVarTester;
        private final Predicate<Object> iterateVarTester;
        private final BiPredicate<Object, Object> overwriteTester;
        private final Predicate<Object> cleanupTester;

        NullPolicy(
                Predicate<Object> initVarTester,
                Predicate<Object> iterateVarTester,
                BiPredicate<Object, Object> overwriteTester,
                Predicate<Object> cleanupTester
        ) {

            this.initVarTester = initVarTester;
            this.iterateVarTester = iterateVarTester;
            this.overwriteTester = overwriteTester;
            this.cleanupTester = cleanupTester;
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

        public void fail() throws NullPointerException {
            throw new NullPointerException(String.format("NullPolicy %s was violated", name()));
        }
    }
}
