package org.comroid.varbind;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

public interface ArrayBind<EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
        extends VarBind<EXTR, DPND, REMAP, FINAL> {
    @Override
    String getFieldName();

    @Override
    Span<EXTR> extract(UniObjectNode node);

    @Override
    REMAP remap(EXTR from, DPND dependency);

    @Override
    FINAL finish(Span<REMAP> parts);

    final class Uno<TARGET, FINAL extends Collection<TARGET>> extends AbstractArrayBind<TARGET, Object, TARGET, FINAL> {
        public Uno(GroupBind group, String fieldName, Function<? extends UniNode, TARGET> extractor, Supplier<FINAL> collectionSupplier) {
            super(group, fieldName, extractor, collectionSupplier);
        }

        @Override
        public TARGET remap(TARGET from, Object dependency) {
            return from;
        }
    }

    final class Duo<EXTR, REMAP, FINAL extends Collection<REMAP>> extends AbstractArrayBind<EXTR, Object, REMAP, FINAL> {
        private final Function<EXTR, REMAP> remapper;

        public Duo(GroupBind group, String fieldName, Function<? extends UniNode, EXTR> extractor, Function<EXTR, REMAP> remapper, Supplier<FINAL> collectionSupplier) {
            super(group, fieldName, extractor, collectionSupplier);

            this.remapper = remapper;
        }

        @Override
        public REMAP remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    final class Dep<EXTR, DPND, REMAP, FINAL extends Collection<REMAP>> extends AbstractArrayBind<EXTR, DPND, REMAP, FINAL> {
        private final BiFunction<EXTR, DPND, REMAP> resolver;

        public Dep(GroupBind group, String fieldName, Function<? extends UniNode, EXTR> extractor, BiFunction<EXTR, DPND, REMAP> resolver, Supplier<FINAL> collectionSupplier) {
            super(group, fieldName, extractor, collectionSupplier);

            this.resolver = resolver;
        }

        @Override
        public REMAP remap(EXTR from, DPND dependency) {
            return resolver.apply(from, Objects.requireNonNull(dependency, "Dependency Object"));
        }
    }
}
