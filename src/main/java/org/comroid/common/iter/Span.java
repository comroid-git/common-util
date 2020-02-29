package org.comroid.common.iter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Span<T> implements Collection<T>, Supplier<T> {
    public static final int DEFAULT_INITIAL_CAPACITY = 1;
    public static final NullPolicy DEFAULT_NULL_POLICY = NullPolicy.IGNORE;
    public static final boolean DEFAULT_ALLOW_STRETCHING = true;
    
    private final NullPolicy nullPolicy;

    private Object[] data;
    private int last;
    private boolean stretch;

    public Span() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_NULL_POLICY, DEFAULT_ALLOW_STRETCHING);
    }

    public Span(int initialCapacity) {
        this(initialCapacity, DEFAULT_NULL_POLICY, DEFAULT_ALLOW_STRETCHING);
    }

    public Span(NullPolicy nullPolicy) {
        this(DEFAULT_INITIAL_CAPACITY, nullPolicy, DEFAULT_ALLOW_STRETCHING);
    }

    public Span(boolean allowStretching) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_NULL_POLICY, allowStretching);
    }

    public Span(int initialCapacity, NullPolicy nullPolicy) {
        this(initialCapacity, nullPolicy, DEFAULT_ALLOW_STRETCHING);
    }

    public Span(int initialCapacity, boolean allowStretching) {
        this(initialCapacity, DEFAULT_NULL_POLICY, allowStretching);
    }

    public Span(NullPolicy nullPolicy, boolean allowStretching) {
        this(DEFAULT_INITIAL_CAPACITY, nullPolicy, allowStretching);
    }

    public Span(int initialCapacity, NullPolicy nullPolicy, boolean allowStretching) {
        this(new Object[initialCapacity], nullPolicy, allowStretching);
    }

    protected Span(Object[] data, NullPolicy nullPolicy, boolean allowStretching) {
        this.nullPolicy = nullPolicy;

        this.data = data;
        this.last = 0;
        this.stretch = allowStretching;
    }

    @Contract("null -> fail; _ -> new")
    public Span<T> policy(NullPolicy nullPolicy) {
        return new Span<>(Arrays.copyOf(data, data.length), Objects.requireNonNull(nullPolicy), true);
    }

    @Override
    @Contract(pure = true)
    public int size() {
        return last;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0 || stream().allMatch(nullPolicy::empty);
    }

    @Override
    public boolean contains(Object o) {
        return stream().anyMatch(o::equals);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Span<T> parent = Span.this;
            private int i = 0;

            @Override
            @Contract(pure = true)
            public boolean hasNext() {
                if (i >= size()) return false;

                return Stream.of(data)
                        .limit(size())
                        .skip(i)
                        .anyMatch(it -> !nullPolicy.skip(it));
            }

            @Override
            public T next() {
                final T value = value(i++);

                if (nullPolicy.skip(value))
                    return next();

                return value;
            }
        };
    }

    @Override
    public @NotNull Object[] toArray() {
        return toArray(new Object[0]);
    }

    @Override
    public @NotNull <A> A[] toArray(@NotNull A[] dummy) {
        //noinspection unchecked
        return stream()
                .limit(size())
                .map(it -> (A) it)
                .toArray(val -> Arrays.copyOf(dummy, val));
    }

    @Override
    @Contract(mutates = "this")
    public boolean add(T item) {
        nullPolicy.require(item);
        int i = 0;

        while (!nullPolicy.empty(value(i))) {
            if (i >= data.length) {
                adjustArray(i);
                break;
            } else i++;
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

        return yield;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    @Contract(mutates = "this")
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return c.stream().anyMatch(this::add);
    }

    @Override
    @Contract(mutates = "this")
    public boolean removeAll(@NotNull Collection<?> c) {
        return c.stream().anyMatch(this::remove);
    }

    @Override
    @Contract(mutates = "this")
    public boolean retainAll(@NotNull Collection<?> c) {
        final List<T> collect = stream()
                .filter(item -> !contains(item))
                .collect(Collectors.toList());

        return removeAll(collect);
    }

    @Override
    @Contract(mutates = "this")
    public void clear() {
        removeAll(this);
    }
    
    @Contract(mutates = "this")
    public void cleanup() {
        data = Stream.of(data)
                .filter(item -> !nullPolicy.empty(item))
                .toArray(Object[]::new);
        last = data.length;
    }

    @Override
    @Contract(pure = true)
    public Stream<T> stream() {
        //noinspection unchecked
        return Stream.of(data)
                .limit(size())
                .map(it -> (T) it);
    }

    @Contract(mutates = "this")
    public boolean append(T item) {
        nullPolicy.require(item);
        this.set(++last, item);

        return true;
    }

    @Contract(mutates = "this")
    public boolean appendAll(@NotNull Collection<? extends T> c) {
        return c.stream().anyMatch(this::append);
    }

    @Override
    public @Nullable T get() {
        return stream().findAny().orElse(null);
    }

    private @Nullable T set(int index, @Nullable T next) {
        final int size = size();

        if (index < 0 || (!stretch && index >= size))
            throw new IndexOutOfBoundsException("Stretching is not allowed here");
        if (stretch) adjustArray(index);

        final T old = value(index);

        if (!nullPolicy.check(old, next))
            nullPolicy.fail();
        data[index] = next;
        
        if (index >= last)
            last = index;
        cleanup();

        return old;
    }

    private int adjustArray(int minSize) {
        if (data.length > minSize)
            return 0;

        int c = 0;
        while (duplex() < minSize)
            c++;
        return c;
    }

    private int duplex() {
        data = Arrays.copyOf(data, intLimit(data.length * 2));

        return data.length;
    }

    private int intLimit(long var) {
        if (var >= Integer.MAX_VALUE) {
            System.err.printf("WARNING: %s is running out of space!", toString());
            return Integer.MAX_VALUE;
        }
        
        return (int) var;
    }

    @SuppressWarnings("unchecked")
    private @Nullable T value(int index) {
        if (index >= size())
            return null;

        return (T) data[index];
    }

    public enum NullPolicy {
        /**
         * Nulls are ignored.
         */
        IGNORE((live, next) -> true),

        /**
         * Skip nulls on iterating actions, but don't prohibit any actions.
         */
        SKIP(IGNORE),

        /**
         * Only overwrite nulls, don't allow nulls to be manually set.
         */
        FILL_ONLY((live, next) -> !Objects.isNull(next)),

        /**
         * Skip nulls on iterating actions and prohibit setting nulls manually.
         */
        SKIP_FILL(FILL_ONLY),

        /**
         * Throw an exception on any null.
         */
        PROHIBIT((live, next) -> !Objects.isNull(live) || !Objects.isNull(next));

        private final static Object dummy = new Object();
        private final BiPredicate<Object, Object> validator;

        NullPolicy(NullPolicy proxy) {
            this(proxy.validator);
        }

        NullPolicy(BiPredicate<Object, Object> validator) {
            this.validator = validator;
        }

        public void fail() throws NullPointerException {
            throw new NullPointerException(String.format("NullPolicy %s was violated", this));
        }

        private void require(Object item) {
            if (empty(item))
                fail();
        }

        private boolean skip(Object any) {
            return this == SKIP && Objects.isNull(any);
        }

        private boolean empty(@Nullable Object any) {
            return Objects.isNull(any);
        }

        private boolean check(@Nullable Object live, @Nullable Object next) {
            return validator.test(live, next);
        }
    }
}
