package org.comroid.common.iter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.comroid.common.ref.Reference;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Span<T> implements AbstractCollection<T>, Reference<T> {
    public static final int        DEFAULT_INITIAL_CAPACITY = 0;
    public static final NullPolicy DEFAULT_NULL_POLICY      = NullPolicy.SKIP_NULLS;
    public static final boolean    DEFAULT_FIXED_SIZE       = false;

    public static <T> Span.API<T> make() {
        return new Span.API<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> Span<T> zeroSize() {
        return (Span<T>) ZeroSize;
    }

    private static final Span<?> ZeroSize = new Span<>(new Object[0], DEFAULT_NULL_POLICY, true);

    //region API Class
    public static final class API<T> {
        private Collection<T> initialValues;
        private NullPolicy    nullPolicy;
        private boolean       fixedSize;

        public API() {
            this.initialValues = new ArrayList<>();
            this.nullPolicy    = DEFAULT_NULL_POLICY;
            this.fixedSize     = DEFAULT_FIXED_SIZE;
        }

        private API(Span<T> base) {
            initialValues = new ArrayList<>(base);
            nullPolicy    = base.nullPolicy;
            fixedSize     = base.fixedSize;
        }

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
        int c = 0;

        for (Object it : data) {
            if (nullPolicy.canIterate(it)) c++;
        }

        return c;
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
    @SuppressWarnings("unchecked")
    public final <R> R[] toArray(@NotNull R[] dummy) {
        return toArray(dummy, it -> (R) it);
    }

    private <R> R[] toArray(R[] dummy, Function<Object, R> castOP) {
        synchronized (dataLock) {
            final R[] yields = Arrays.copyOf(dummy, size());

            for (int i, c = i = 0; i < data.length; i++) {
                final T valueAt = valueAt(i);

                if (nullPolicy.canIterate(valueAt)) yields[c++] = castOP.apply(valueAt);
            }

            return yields;
        }
    }

    private @Nullable T valueAt(int index) {
        synchronized (dataLock) {
            if (index >= size()) return null;

            @SuppressWarnings("unchecked") T cast = (T) data[index];
            return nullPolicy.canIterate(cast) ? cast : null;
        }
    }

    @Override
    public final synchronized boolean add(T it) {
        synchronized (dataLock) {
            int i;

            for (i = 0; i < data.length; i++) {
                if (nullPolicy.canOverwrite(valueAt(i), it)) {
                    data[i] = it;
                    return true;
                }
            }

            if (fixedSize) throw new IndexOutOfBoundsException("Span cannot be resized");
            if (i != data.length) throw new AssertionError(String.format(
                    "Suspicious Span.add() call: index too large {expected: %d, actual: %d}%n",
                    data.length,
                    i
            ));

            if (!nullPolicy.canInitialize(it)) return false;

            // array too small
            data    = Arrays.copyOf(data, i + 1);
            data[i] = it;
            return true;
        }
    }

    @Override
    public final synchronized boolean remove(Object other) {
        synchronized (dataLock) {
            if (fixedSize) {
                for (int i = 0; i < data.length; i++) {
                    final T valueAt = valueAt(i);

                    if (!nullPolicy.canIterate(valueAt)) continue;

                    if (other.equals(valueAt) && nullPolicy.canOverwrite(valueAt, null)) {
                        data[i] = null;
                        return true;
                    }
                }
            } else {
                final int           ol      = data.length;
                final Collection<T> newData = new ArrayList<>();

                for (int i = 0; i < data.length; i++) {
                    final T valueAt = valueAt(i);

                    if (!nullPolicy.canIterate(valueAt)) continue;

                    if (other.equals(valueAt)) {
                        if (!nullPolicy.canOverwrite(valueAt, null)) nullPolicy.fail(String.format("Cannot remove %s from Span",
                                                                                                   valueAt
                        ));
                    } else newData.add(valueAt);
                }

                data = newData.toArray();
                return ol != data.length;
            }

            return false;
        }
    }

    @Override
    public final void clear() {
        synchronized (dataLock) {
            if (fixedSize) Arrays.fill(data, NullPolicy.dummy);
            else data = new Object[0];
        }
    }

    private final NullPolicy nullPolicy;
    private final Object     dataLock = new Object() {
        private volatile Object selfaware_keepalive = Span.this.dataLock;
    };
    private       Object[]   data;
    private       boolean    fixedSize;

    public Span() {
        this(new Object[DEFAULT_INITIAL_CAPACITY], DEFAULT_NULL_POLICY, DEFAULT_FIXED_SIZE);
    }

    protected Span(Object[] data, NullPolicy nullPolicy, boolean fixedSize) {
        this.nullPolicy = nullPolicy;

        this.data      = data;
        this.fixedSize = fixedSize;
    }

    @Nullable
    @Override
    public final T get() {
        synchronized (dataLock) {
            for (int i = 0; i < data.length; i++) {
                final T valueAt = valueAt(i);

                if (nullPolicy.canIterate(valueAt))
                    return valueAt;
            }

            return null;
        }
    }

    @Override
    public @NotNull T requireNonNull() throws NullPointerException {
        return requireNonNull("No iterable value present");
    }

    public final API<T> reconfigure() {
        return new API<>(this);
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
        if (fixedSize) {
            data = stream().filter(it -> !nullPolicy.canCleanup(it))
                           .toArray(Object[]::new);
        } else data = toArray();
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum NullPolicy {
        //endformatting
        SKIP_NULLS(
                init -> nonNull(init),
                iterate -> nonNull(iterate),
                (overwriting, with) -> isNull(overwriting),
                cleanup -> isNull(cleanup)
        ),

        NULL_ON_INIT(
                init -> nonNull(init),
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with) && isNull(overwriting),
                cleanup -> isNull(cleanup)
        ),

        PROHIBIT_NULLS(
                init -> {
                    Objects.requireNonNull(init);
                    return true;
                },
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with) && isNull(overwriting),
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
            return var != dummy && initVarTester.test(var);
        }

        public boolean canIterate(Object var) {
            return var != dummy && iterateVarTester.test(var);
        }

        public boolean canOverwrite(Object old, Object with) {
            return (old != dummy && with != dummy) && overwriteTester.test(old, with);
        }

        public boolean canCleanup(Object var) {
            return var != dummy && cleanupTester.test(var);
        }

        public void fail(String message) throws NullPointerException {
            throw new NullPointerException(String.format("NullPolicy %s was violated: %s",
                                                         name(),
                                                         message
            ));
        }
    }

    public final class Iterator implements java.util.Iterator<T> {
        private final     Object[] dataSnapshot  = toArray();
        private           int      previousIndex = 0;
        private @Nullable Object   next;

        @Override
        public boolean hasNext() {
            return next != null || tryAcquireNext();
        }

        @Override
        @SuppressWarnings("unchecked")
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
            if (next != null || nextIndex >= dataSnapshot.length) return false;

            next = dataSnapshot[nextIndex];

            while (!nullPolicy.canIterate(next)) {
                tryAcquireNext();
            }

            previousIndex = nextIndex;
            return true;
        }
    }
}
