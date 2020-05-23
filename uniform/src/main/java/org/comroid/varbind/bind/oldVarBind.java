package org.comroid.varbind.bind;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.model.AbstractObjectBind;
import org.comroid.varbind.model.Reprocessed;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface oldVarBind<EXTR, DPND, REMAP, FINAL> extends GroupedBind<DPND> {
    String getFieldName();

    default FINAL getFrom(UniObjectNode node) {
        return getFrom(null, node);
    }

    default FINAL getFrom(DPND dependencyObject, UniObjectNode node) {
        return process(dependencyObject, extract(node));
    }

    default FINAL process(final DPND dependency, Span<EXTR> from) {
        return finish(remapAll(dependency, from));
    }

    Span<EXTR> extract(UniObjectNode node);

    FINAL finish(Span<REMAP> parts);

    default Span<REMAP> remapAll(final DPND dependency, Span<EXTR> from) {
        return from.pipe()
                .map(each -> remap(each, dependency))
                .span();
    }

    REMAP remap(EXTR from, DPND dependency);

    default <R> ReBind.TwoStage<FINAL, R> rebindSimple(Function<FINAL, R> remapper) {
        return rebindSimple(getGroup(), remapper);
    }

    default <R> ReBind.TwoStage<FINAL, R> rebindSimple(GroupBind group, Function<FINAL, R> remapper) {
        return new ReBind.TwoStage<>(Polyfill.uncheckedCast(this), group, remapper);
    }

    default <R, D extends DPND> ReBind.DependentTwoStage<FINAL, D, R> rebindDependent(BiFunction<FINAL, D, R> resolver) {
        return rebindDependent(getGroup(), resolver);
    }

    default <R, D extends DPND> ReBind.DependentTwoStage<FINAL, D, R> rebindDependent(GroupBind group, BiFunction<FINAL, D, R> resolver) {
        return new ReBind.DependentTwoStage<>(Polyfill.uncheckedCast(this), group, resolver);
    }

    final class OneStage<TARGET> extends AbstractObjectBind<TARGET, Object, TARGET> {
        public OneStage(
                GroupBind group, String fieldName, BiFunction<UniObjectNode, String, TARGET> extractor
        ) {
            super(group, fieldName, extractor.andThen(Span::singleton));
        }

        @Override
        public TARGET remap(TARGET from, Object dependency) {
            return from;
        }
    }

    final class TwoStage<EXTR, TARGET> extends AbstractObjectBind<EXTR, Object, TARGET> {
        private final Function<EXTR, TARGET> remapper;

        public TwoStage(
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

    final class DependentTwoStage<EXTR, DPND, TARGET> extends AbstractObjectBind<EXTR, DPND, TARGET> {
        private final BiFunction<DPND, EXTR, TARGET> resolver;

        public DependentTwoStage(
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
}
