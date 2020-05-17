package org.comroid.varbind.bind;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.iter.span.BasicSpan;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.model.AbstractArrayBind;

public interface ArrayBind<EXTR, DPND, REMAP, FINAL extends Collection<REMAP>> extends VarBind<EXTR, DPND, REMAP, FINAL> {
    @Override
    String getFieldName();

    @Override
    BasicSpan<EXTR> extract(UniObjectNode node);

    @Override
    FINAL finish(BasicSpan<REMAP> parts);

    @Override
    REMAP remap(EXTR from, DPND dependency);

    final class OneStage<TARGET, FINAL extends Collection<TARGET>> extends AbstractArrayBind<TARGET, Object, TARGET, FINAL> {
        public OneStage(
                GroupBind group,
                String fieldName,
                Function<? extends UniNode, TARGET> extractor,
                Supplier<FINAL> collectionSupplier
        ) {
            super(group, fieldName, extractor, collectionSupplier);
        }

        @Override
        public TARGET remap(TARGET from, Object dependency) {
            return from;
        }
    }

    final class TwoStage<EXTR, REMAP, FINAL extends Collection<REMAP>> extends AbstractArrayBind<EXTR, Object, REMAP, FINAL> {
        private final Function<EXTR, REMAP> remapper;

        public TwoStage(
                GroupBind group,
                String fieldName,
                Function<? extends UniNode, EXTR> extractor,
                Function<EXTR, REMAP> remapper,
                Supplier<FINAL> collectionSupplier
        ) {
            super(group, fieldName, extractor, collectionSupplier);

            this.remapper = remapper;
        }

        @Override
        public REMAP remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    final class DependentTwoStage<EXTR, DPND, REMAP, FINAL extends Collection<REMAP>> extends AbstractArrayBind<EXTR, DPND, REMAP, FINAL> {
        private final BiFunction<DPND, EXTR, REMAP> resolver;

        public DependentTwoStage(
                GroupBind group,
                String fieldName,
                Function<? extends UniNode, EXTR> extractor,
                BiFunction<DPND, EXTR, REMAP> resolver,
                Supplier<FINAL> collectionSupplier
        ) {
            super(group, fieldName, extractor, collectionSupplier);

            this.resolver = resolver;
        }

        @Override
        public REMAP remap(EXTR from, DPND dependency) {
            return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object"), from);
        }
    }
}
