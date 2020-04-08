package org.comroid.varbind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

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
public interface VarBind<EXTR, DPND, REMAP, FINAL> extends GroupedBind {
    String getFieldName();

    Span<EXTR> extract(UniObjectNode node);

    default FINAL process(final DPND dependency, Span<EXTR> from) {
        return finish(remapAll(dependency, from));
    }

    default Span<REMAP> remapAll(final DPND dependency, Span<EXTR> from) {
        return from.stream()
                .map(each -> remap(each, dependency))
                .collect(Span.collector());
    }

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
    @interface Root {
    }

    /**
     * Variable definition with 0 mapping Stages.
     *
     * @param <NODE> Serialization Library Type of the serialization Node
     * @param <S>    The serialization input & output Type
     */
    final class Uno<TARGET> extends AbstractObjectBind<TARGET, Object, TARGET> {
        public Uno(GroupBind group, String fieldName, BiFunction<UniNode, String, TARGET> extractor) {
            super(group, fieldName, extractor.andThen(Span::singleton));
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
    final class Duo<EXTR, TARGET> extends AbstractObjectBind<EXTR, Object, TARGET> {
        private final Function<EXTR, TARGET> remapper;

        public Duo(GroupBind group, String fieldName, BiFunction<UniNode, String, EXTR> extractor, Function<EXTR, TARGET> remapper) {
            super(group, fieldName, extractor.andThen(Span::singleton));

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
    final class Dep<EXTR, DPND, TARGET> extends AbstractObjectBind<EXTR, DPND, TARGET> {
        private final BiFunction<EXTR, DPND, TARGET> resolver;

        public Dep(GroupBind group, String fieldName, BiFunction<UniNode, String, EXTR> extractor, BiFunction<EXTR, DPND, TARGET> resolver) {
            super(group, fieldName, extractor.andThen(Span::singleton));

            this.resolver = resolver;
        }

        @Override
        public TARGET remap(EXTR from, DPND dependency) {
            return resolver.apply(from, Objects.requireNonNull(dependency, "Dependency Object"));
        }
    }
}
