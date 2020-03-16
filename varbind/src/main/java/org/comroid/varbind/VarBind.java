package org.comroid.varbind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.varbind.model.VariableCarrier;

import org.jetbrains.annotations.Nullable;

/**
 * Basic Variable Binding definition
 * Serves as an interface to handling data when serializing.
 *
 * @param <S>    The (singular) remapping input Type
 * @param <A>    The (singular) remapping output Type
 * @param <D>    The (singular) dependency Type; {@link Void} is default for {@code independent}
 * @param <R>    The (singular) output Type, this is what you get from {@link VariableCarrier#getVar(VarBind)}
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

        String rootNode() default "";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Root {
    }

    //region Types
    /**
     * Variable definition with 0 mapping Stages.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input & output Type
     */
    final class Uno<NODE, S> extends AbstractObjectBind<S, S, Void, S, NODE> {
        Uno(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor) {
            super(group, name, extractor.andThen(Span::fixedSize));
        }

        @Override
        public final S remap(S from, @Nullable Void dependency) {
            return from;
        }

        @Override
        public final S finish(Span<S> parts) {
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
    final class Duo<NODE, S, A> extends AbstractObjectBind<S, A, Void, A, NODE> {
        private final Function<S, A> remapper;

        Duo(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, Function<S, A> remapper) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.remapper = remapper;
        }

        @Override
        public final A remap(S from, Void dependency) {
            return remapper.apply(from);
        }

        @Override
        public final A finish(Span<A> parts) {
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
    final class Dep<NODE, S, A, D> extends AbstractObjectBind<S, A, D, A, NODE> {
        private final BiFunction<D, S, A> resolver;

        Dep(@Nullable GroupBind group, String name, BiFunction<NODE, String, S> extractor, BiFunction<D, S, A> resolver) {
            super(group, name, extractor.andThen(Span::fixedSize));

            this.resolver = resolver;
        }

        @Override
        public final A remap(S from, D dependency) {
            return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object is null"), from);
        }

        @Override
        public final A finish(Span<A> parts) {
            if (parts.isSingle())
                return parts.get();

            throw new AssertionError("Span too large");
        }
    }
    //endregion
}
