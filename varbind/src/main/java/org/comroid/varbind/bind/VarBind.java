package org.comroid.varbind.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.varbind.model.VariableCarrier;

import org.jetbrains.annotations.Nullable;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

/**
 * Basic Variable Binding definition
 * Serves as an interface to handling data when serializing.
 *
 * @param <S>    The (singular) remapping input Type
 * @param <A>    The (singular) remapping output Type
 * @param <D>    The (singular) dependency Type; {@link Void} is default for {@code independent}
 * @param <R>    The (singular) output Type
 * @param <NODE> Serialization Library Type of the serialization Node
 */
public interface VarBind<S, A, D, R, NODE> extends GroupedBind {
    Span<? super S> extract(NODE node);

    A remap(S from, D dependency);

    R finish(Span<A> parts);

    String getName();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Location {
        Class<?> value();
    }

    abstract class Abstract<S, A, D, R, NODE> implements VarBind<S, A, D, R, NODE> {
        private final String name;
        private final @Nullable GroupBind group;
        private final BiFunction<NODE, String, Span<S>> extractor;

        protected Abstract(@Nullable GroupBind group, String name, BiFunction<NODE, String, Span<S>> extractor) {
            this.name = name;
            this.group = group;
            this.extractor = extractor;
        }

        @Override
        public final Optional<GroupBind> getGroup() {
            return Optional.ofNullable(group);
        }

        @Override
        public final Span<S> extract(NODE node) {
            return extractor.apply(node, name);
        }

        @Override
        public final String getName() {
            return name;
        }
    }

    //region Types

    /**
     * Variable definition with 0 mapping Stages.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input & output Type
     */
    final class Uno<NODE, S> extends Abstract<S, S, Void, S, NODE> {
        public Uno(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor) {
            super(group, name, extractor.andThen(Span::fixedSize));
        }

        @Override
        public S remap(S from, @Nullable Void dependency) {
            return from;
        }

        @Override
        public S finish(Span<S> parts) {
            if (parts.isSingle())
                return parts.get();

            throw new AssertionError("Span too large");
        }
    }

    /**
     * Variable definition with 1 mapping Stage.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     */
    final class Duo<NODE, S, A> extends Abstract<S, A, Void, A, NODE> {
        private final Function<S, A> remapper;

        public Duo(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, Function<S, A> remapper) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.remapper = remapper;
        }

        @Override
        public A remap(S from, Void dependency) {
            return remapper.apply(from);
        }

        @Override
        public A finish(Span<A> parts) {
            if (parts.isSingle())
                return parts.get();

            throw new AssertionError("Span too large");
        }
    }

    /**
     * Variable definition with 2 mapping Stages, one of which uses an environmentally global variable.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     * @param <D>    The dependency Type
     *
     * @see VariableCarrier Dependency Type
     */
    final class Dep<NODE, S, A, D> extends Abstract<S, A, D, A, NODE> {
        private final BiFunction<D, S, A> resolver;

        public Dep(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, BiFunction<D, S, A> resolver) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.resolver = resolver;
        }

        @Override
        public A remap(S from, D dependency) {
            return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object is null"), from);
        }

        @Override
        public A finish(Span<A> parts) {
            if (parts.isSingle())
                return parts.get();

            throw new AssertionError("Span too large");
        }
    }

    /**
     * {@link Collection} building Variable definition with 1 mapping Stage.
     * Used for deserializing arrays of data.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     *
     * @see Duo
     */
    final class LsDuo<NODE, S, A> extends Abstract<S, A, Void, Collection<A>, NODE> {
        private final Function<S, A> remapper;

        public LsDuo(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, Function<S, A> remapper) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.remapper = remapper;
        }

        @Override
        public A remap(S from, Void dependency) {
            return remapper.apply(from);
        }

        @Override
        public Collection<A> finish(Span<A> parts) {
            return unmodifiableCollection(parts);
        }
    }

    /**
     * {@link Collection} building Variable definition with 2 mapping Stages.
     * Used for deserializing arrays of data.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     * @param <D>    The dependency Type
     *
     * @see Dep
     */
    final class LsDep<NODE, S, A, D> extends Abstract<S, A, D, Collection<A>, NODE> {
        private final BiFunction<D, S, A> resolver;

        public LsDep(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, BiFunction<D, S, A> resolver) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.resolver = resolver;
        }

        @Override
        public A remap(S from, D dependency) {
            return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object is null"), from);
        }

        @Override
        public Collection<A> finish(Span<A> parts) {
            return unmodifiableCollection(parts);
        }
    }

    /**
     * {@link Set} building Variable definition with 2 mapping Stages.
     * Used for deserializing arrays of data.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     * @param <D>    The dependency Type
     *
     * @see Dep
     */
    final class LsIdn<NODE, S, A, D> extends Abstract<S, A, D, Set<A>, NODE> {
        private final BiFunction<D, S, A> resolver;

        public LsIdn(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, BiFunction<D, S, A> resolver) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.resolver = resolver;
        }

        @Override
        public A remap(S from, D dependency) {
            return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object is null"), from);
        }

        @Override
        public Set<A> finish(Span<A> parts) {
            return unmodifiableSet(new HashSet<>(parts));
        }
    }
    //endregion
}
