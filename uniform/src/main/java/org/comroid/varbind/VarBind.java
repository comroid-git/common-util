package org.comroid.varbind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniObjectNode;

public interface VarBind<EXTR, DPND, REMAP, FINAL> extends GroupedBind {
    final class Uno<TARGET> extends AbstractObjectBind<TARGET, Object, TARGET> {
        public Uno(
                GroupBind group, String fieldName, BiFunction<UniObjectNode, String, TARGET> extractor
        ) {
            super(group, fieldName, extractor.andThen(Span::singleton));
        }

        @Override
        public TARGET remap(TARGET from, Object dependency) {
            return from;
        }
    }

    final class Duo<EXTR, TARGET> extends AbstractObjectBind<EXTR, Object, TARGET> {
        private final Function<EXTR, TARGET> remapper;

        public Duo(
                GroupBind group,
                String fieldName,
                BiFunction<UniObjectNode, String, EXTR> extractor,
                Function<EXTR, TARGET> remapper
        ) {
            super(group, fieldName, extractor.andThen(Span::singleton));

            this.remapper = remapper;
        }

        @Override
        public TARGET remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    final class Dep<EXTR, DPND, TARGET> extends AbstractObjectBind<EXTR, DPND, TARGET> {
        private final BiFunction<DPND, EXTR, TARGET> resolver;

        public Dep(
                GroupBind group,
                String fieldName,
                BiFunction<UniObjectNode, String, EXTR> extractor,
                BiFunction<DPND, EXTR, TARGET> resolver
        ) {
            super(group, fieldName, extractor.andThen(Span::singleton));

            this.resolver = resolver;
        }

        @Override
        public TARGET remap(EXTR from, DPND dependency) {
            return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object"), from);
        }
    }

    String getFieldName();

    Span<EXTR> extract(UniObjectNode node);

    default FINAL process(final DPND dependency, Span<EXTR> from) {
        return finish(remapAll(dependency, from));
    }

    FINAL finish(Span<REMAP> parts);

    default Span<REMAP> remapAll(final DPND dependency, Span<EXTR> from) {
        return from.stream()
                .map(each -> remap(each, dependency))
                .collect(Span.collector());
    }

    REMAP remap(EXTR from, DPND dependency);

    default <R> ReBind.Duo<FINAL, R> rebindSimple(Function<FINAL, R> remapper) {
        return rebindSimple(getGroup(), remapper);
    }

    default <R> ReBind.Duo<FINAL, R> rebindSimple(GroupBind group, Function<FINAL, R> remapper) {
        return new ReBind.Duo<>(Polyfill.deadCast(this), group, remapper);
    }

    default <R, D extends DPND> ReBind.Dep<FINAL, D, R> rebindDependent(BiFunction<FINAL, D, R> resolver) {
        return rebindDependent(getGroup(), resolver);
    }

    default <R, D extends DPND> ReBind.Dep<FINAL, D, R> rebindDependent(GroupBind group, BiFunction<FINAL, D, R> resolver) {
        return new ReBind.Dep<>(Polyfill.deadCast(this), group, resolver);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Location {
        Class<?> value();

        String rootNode() default "";
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Root {}

    interface NotAutoprocessed {
    }
}
