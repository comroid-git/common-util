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
    public static final int          DEFAULT_INITIAL_CAPACITY = 0;
    public static final ModifyPolicy DEFAULT_MODIFY_POLICY    = ModifyPolicy.SKIP_NULLS;
    public static final boolean      DEFAULT_FIXED_SIZE       = false;

    public static <T> Collector<T, ?, Span<T>> collector() {
        return Span.<T>make()
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .collector();
    }

    public static <T> Span.API<T> make() {
        return new Span.API<>();
    }

    public static <T> Span<T> zeroSize() {
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

    private final ModifyPolicy modifyPolicy;
    private final Object       dataLock = new Object() {
        private volatile Object selfaware_keepalive = Span.this.dataLock;
    };
    //endregion
    private       Object[]     data;
    private       boolean      fixedSize;

    public Span() {
        this(new Object[DEFAULT_INITIAL_CAPACITY], DEFAULT_MODIFY_POLICY, DEFAULT_FIXED_SIZE);
    }

    public Span(int capacity, boolean fixedSize) {
        this(new Object[capacity], DEFAULT_MODIFY_POLICY, fixedSize);
    }

    protected Span(Object[] data, ModifyPolicy modifyPolicy, boolean fixedSize) {
        this.modifyPolicy = modifyPolicy;

        this.data = data;
        this.fixedSize = fixedSize;
    }

    @Contract(mutates = "this")
    public void sort(Comparator<T> comparator) {
        synchronized (dataLock) {
            data = stream().sorted(comparator)
                    .toArray();
        }

        cleanup();
    }

    private <R> R[] toArray(R[] dummy, Function<Object, R> castOP) {
        synchronized (dataLock) {
            final R[] yields = Arrays.copyOf(dummy, size());

            for (int i, c = i = 0; i < data.length; i++) {
                final T valueAt = valueAt(i);

                if (modifyPolicy.canIterate(valueAt)) yields[c++] = castOP.apply(valueAt);
            }

            return yields;
        }
    }

    private @Nullable T valueAt(int index) {
        synchronized (dataLock) {
            if (index >= size()) return null;

            T cast = (T) data[index];
            return modifyPolicy.canIterate(cast) ? cast : null;
        }
    }

    public final boolean isSingle() {
        return size() == 1;
    }

    @Override
    @Contract
    public final int size() {
        synchronized (dataLock) {
            int c = 0;

            for (Object it : data) {
                if (modifyPolicy.canIterate(it)) c++;
            }

            return c;
        }
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

            for (i = 0; i < data.length; i++) {
                if (modifyPolicy.canOverwrite(valueAt(i), it)) {
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

            if (!modifyPolicy.canInitialize(it)) return false;

            // array too small
            data = Arrays.copyOf(data, i + 1);
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

                    if (!modifyPolicy.canIterate(valueAt)) continue;

                    if (other.equals(valueAt) && modifyPolicy.canRemove(valueAt)) {
                        data[i] = null;
                        return true;
                    }
                }
            } else {
                final int           ol      = data.length;
                final Collection<T> newData = new ArrayList<>();

                for (int i = 0; i < data.length; i++) {
                    final T valueAt = valueAt(i);

                    if (!modifyPolicy.canIterate(valueAt)) continue;

                    if (other.equals(valueAt)) {
                        if (!modifyPolicy.canRemove(valueAt)) {
                            modifyPolicy.fail(String.format("Cannot remove %s from Span", valueAt));
                        }
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
            if (fixedSize) Arrays.fill(data, ModifyPolicy.dummy);
            else data = new Object[0];
        }
    }

    @Override
    public final String toString() {
        @NotNull Object[] arr = toArray();
        return String.format("Span{nullPolicy=%s, data={%d}%s}",
                modifyPolicy,
                arr.length,
                Arrays.toString(arr)
        );
    }

    @Nullable
    @Override
    public final T get() {
        synchronized (dataLock) {
            for (int i = 0; i < data.length; i++) {
                final T valueAt = valueAt(i);

                if (modifyPolicy.canIterate(valueAt)) return valueAt;
            }

            return null;
        }
    }

    @Override
    public @NotNull T requireNonNull() throws NullPointerException {
        return requireNonNull("No iterable value present");
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

    @Contract(mutates = "this")
    public final synchronized void cleanup() {
        if (fixedSize) {
            data = stream().filter(it -> !modifyPolicy.canCleanup(it))
                    .toArray(Object[]::new);
        } else data = toArray();
    }

    private static final Span<?> ZeroSize = new Span<>(new Object[0], ModifyPolicy.IMMUTABLE, true);

    //region API Class
    public static final class API<T> {
        private Collection<T> initialValues;
        private ModifyPolicy  modifyPolicy;
        private boolean       fixedSize;

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
                private final Supplier<Span<T>>          supplier    = Span::new;
                private final BiConsumer<Span<T>, T>     accumulator = Span::add;
                private final BinaryOperator<Span<T>>    combiner    = (ts, ts2) -> {
                    ts.addAll(ts2);

                    return ts;
                };
                private final Collection<T>              initialValues;
                private final ModifyPolicy               nullPolicy;
                private final boolean                    fixedSize;
                private final Function<Span<T>, Span<T>> finisher    = new Function<Span<T>, Span<T>>() {
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
        private final     Object[] dataSnapshot  = toArray();
        private           int      previousIndex = -1;
        private @Nullable Object   next;

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
            if (next != null || nextIndex >= dataSnapshot.length) return false;

            next = dataSnapshot[nextIndex];

            while (!modifyPolicy.canIterate(next)) {
                tryAcquireNext();
            }

            previousIndex = nextIndex;
            return next != null;
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum ModifyPolicy {
        //endformatting
        SKIP_NULLS(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> isNull(overwriting),
                remove -> true,
                cleanup -> isNull(cleanup)
        ),

        NULL_ON_INIT(
                init -> true,
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with) && isNull(overwriting),
                remove -> true,
                cleanup -> isNull(cleanup)
        ),

        PROHIBIT_NULLS(
                init -> {
                    Objects.requireNonNull(init);
                    return true;
                },
                iterate -> nonNull(iterate),
                (overwriting, with) -> nonNull(with) && isNull(overwriting),
                remove -> true,
                cleanup -> isNull(cleanup)
        ),

        IMMUTABLE(
                init -> true,
                iterate -> true,
                (overwriting, with) -> false,
                remove -> false,
                cleanup -> false
        );
        //startformatting

        private final Predicate<Object>           initVarTester;
        private final Predicate<Object>           iterateVarTester;
        private final BiPredicate<Object, Object> overwriteTester;
        private final Predicate<Object>           removeTester;
        private final Predicate<Object>           cleanupTester;

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
            throw new NullPointerException(String.format("NullPolicy %s was violated: %s",
                    name(),
                    message
            ));
        }

        private final static Object dummy = new Object();
    }
}
