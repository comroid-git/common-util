package org.comroid.varbind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;

import org.jetbrains.annotations.Nullable;

/**
 * Basic Variable Binding definition Serves as an interface to handling data when serializing.
 *
 * @param <S>    The (singular) remapping input Type
 * @param <A>    The (singular) remapping output Type
 * @param <D>    The (singular) dependency Type; {@link Void} is default for {@code independent}
 * @param <R>    The (singular) output Type, this is what you get from {@link
 *               VariableCarrier#getVar(VarBind)}
 * @param <NODE> Serialization Library Type of the serialization Node
 */
public interface VarBind<NODE, EXTR, DPND, REMAP, FINAL> extends GroupedBind {
    /**
     * Variable definition with 0 mapping Stages.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input & output Type
     */
    final class Uno<NODE, TARGET> extends AbstractObjectBind<NODE, TARGET, Object, TARGET> {
        protected Uno(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, Span<TARGET>> extractor
        ) {
            super(seriLib, group, name, extractor);
        }

        @Override
        public TARGET remap(TARGET from, Object dependency) {
            return from;
        }
    }

    /**
     * Variable definition with 1 mapping Stage.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     */
    final class Duo<NODE, EXTR, TARGET> extends AbstractObjectBind<NODE, EXTR, Object, TARGET> {
        private final Function<EXTR, TARGET> remapper;

        protected Duo(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, Span<EXTR>> extractor,
                Function<EXTR, TARGET> remapper
        ) {
            super(seriLib, group, name, extractor);

            this.remapper = remapper;
        }

        @Override
        public TARGET remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    /**
     * Variable definition with 2 mapping Stages, one of which uses an environmentally global
     * variable.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input Type
     * @param <A>    The mapping output Type
     * @param <D>    The dependency Type
     *
     * @see VariableCarrier Dependency Type
     */
    final class Dep<NODE, EXTR, DPND, TARGET> extends AbstractObjectBind<NODE, EXTR, DPND, TARGET> {
        private final BiFunction<EXTR, DPND, TARGET> remapper;

        protected Dep(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, Span<EXTR>> extractor,
                BiFunction<EXTR, DPND, TARGET> remapper
        ) {
            super(seriLib, group, name, extractor);
            this.remapper = remapper;
        }

        @Override
        public TARGET remap(EXTR from, DPND dependency) {
            return remapper.apply(from,
                                  Objects.requireNonNull(
                                          dependency,
                                          "Dependency Object " + "Required"
                                  )
            );
        }
    }

    String getName();

    Span<EXTR> extract(NODE node);

    REMAP remap(EXTR from, DPND dependency);

    FINAL finish(Span<REMAP> parts);

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Location {
        Class<?> value();

        String rootNode() default "";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Root {}
}
