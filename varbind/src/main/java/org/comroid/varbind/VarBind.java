package org.comroid.varbind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

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
    String getName();

    default Span<EXTR> extract(UniNode<Object, NODE> node) {
        switch (node.getType()) {
            case OBJECT:
                final UniObjectNode uniObject = (UniObjectNode) node;
                final String key = getName();

                if (!uniObject.containsKey(key))
                    return Span.zeroSize();

                final EXTR extracted = (EXTR) uniObject.get(key);

                return Span.singleton(extracted);
            case ARRAY:
                throw new IllegalArgumentException("VarBind cannot extract from Array node");
        }

        throw new AssertionError();
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
    final class Uno<NODE, TARGET> extends AbstractObjectBind<NODE, TARGET, Object, TARGET> {
        protected Uno(
                @Nullable GroupBind group,
                String name,
                BiFunction<? super UniObjectNode<? super NODE, ?, ? super TARGET>, String, TARGET> extractor
        ) {
            super(group, name, extractor);
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
                @Nullable GroupBind group,
                String name,
                BiFunction<? super UniObjectNode<? super NODE, ?, ? super EXTR>, String, EXTR> extractor,
                Function<EXTR, TARGET> remapper
        ) {
            super(group, name, extractor);

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
                @Nullable GroupBind group,
                String name,
                BiFunction<? super UniObjectNode<? super NODE, ?, ? super EXTR>, String, EXTR> extractor,
                BiFunction<EXTR, DPND, TARGET> remapper
        ) {
            super(group, name, extractor);

            this.remapper = remapper;
        }

        @Override
        public TARGET remap(EXTR from, DPND dependency) {
            return remapper.apply(from,
                    Objects.requireNonNull(dependency,
                            "Dependency Object " + "Required"
                    )
            );
        }
    }
}
