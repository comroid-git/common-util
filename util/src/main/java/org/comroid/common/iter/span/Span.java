package org.comroid.common.iter.span;

import org.comroid.common.iter.ReferenceIndex;
import org.comroid.common.iter.pipe.Pipe;
import org.comroid.common.ref.Reference;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.Objects.nonNull;

public interface Span<T> extends Collection<T>, ReferenceIndex<T>, Reference<T> {
    int SIZE_UNFIXED = -1;
    ModifyPolicy DEFAULT_MODIFY_POLICY = ModifyPolicy.SKIP_NULLS;

    boolean isSingle();

    boolean isNotSingle();

    boolean isFixedSize();

    static <T> Collector<T, ?, Span<T>> collector() {
        return Span.<T>make().fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .collector();
    }

    static <T> API<T> make() {
        return new API<>();
    }

    static <T> Span<T> empty() {
        //noinspection unchecked
        return (Span<T>) BasicSpan.EMPTY;
    }

    static <T> Span<T> singleton(T it) {
        return Span.<T>make().initialValues(it)
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
    }

    static <T> Span<T> immutable(Collection<T> of) {
        return Span.<T>make().initialValues(of)
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
    }

    @SafeVarargs
    static <T> Span<T> immutable(T... of) {
        return Span.<T>make().initialValues(of)
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
    }

    @Override
    @Contract
    int size();

    @NotNull BasicSpan.Iterator iterator();

    @NotNull
    @SuppressWarnings("NullableProblems")
        // false positive
    Object[] toArray();

    @SuppressWarnings("NullableProblems")
        // false positive
    <R> @NotNull R[] toArray(@NotNull R[] dummy);

    @Override
    boolean add(T it);

    @Override
    boolean remove(Object other);

    @Override
    void clear();

    @Override
    Pipe<?, T> pipe();

    @Override
    Reference<T> getReference(int index);

    @Override
    String toString();

    @Override
    T get();

    @Override
    @NotNull
    T requireNonNull() throws NullPointerException;

    Span<T> range(int startIncl, int endExcl);

    @Contract(mutates = "this")
    void cleanup();

    @Contract("-> new")
    API<T> reconfigure(/* todo: boolean parameter finalizeOldSpan? */);

    <C extends Collection<T>> C into(Supplier<C> collectionSupplier);

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

    final class API<T> {
        private final Span<T> base;
        private Collection<T> initialValues;
        private ModifyPolicy modifyPolicy;
        private boolean fixedSize;

        public API() {
            this(new BasicSpan<>());
        }

        API(Span<T> base) {
            this.base = base;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public API<T> initialSize(int initialSize) {
            this.initialValues = new ArrayList<>(initialSize);

            return this;
        }

        public Collector<T, ?, Span<T>> collector() {
            class SpanCollector implements Collector<T, Span<T>, Span<T>> {
                private final Supplier<Span<T>> supplier = BasicSpan::new;
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
            if (fixedSize) {
                if (base.isFixedSize())
                    throw new IllegalArgumentException("Base is of fixed size!");

                return new BasicSpan<>(base, base.size(), modifyPolicy);
            } else return new BasicSpan<>(base, modifyPolicy);
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
}
