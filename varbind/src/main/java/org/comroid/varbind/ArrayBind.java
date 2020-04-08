package org.comroid.varbind;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.common.iter.Span.ModifyPolicy;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;

import org.jetbrains.annotations.Nullable;

public interface ArrayBind<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
        extends VarBind<NODE, EXTR, DPND, REMAP, FINAL> {
    @Override
    String getName();

    @Override
    Span<EXTR> extract(UniNode node);

    @Override
    REMAP remap(EXTR from, DPND dependency);

    @Override
    default FINAL finish(Span<REMAP> parts) {
        return (FINAL) parts.reconfigure()
                .fixedSize(true)
                .modifyPolicy(ModifyPolicy.IMMUTABLE)
                .span();
    }

    final class Uno<NODE, TARGET, FINAL extends Collection<TARGET>>
            extends AbstractArrayBind<NODE, TARGET, Object, TARGET, FINAL> {
        protected Uno(
                @Nullable GroupBind group,
                String name,
                BiFunction<UniArrayNode, String, Collection<TARGET>> extractor,
                Function<Span<TARGET>, FINAL> collectionFinalizer
        ) {
            super(group, name, extractor, collectionFinalizer);
        }

        @Override
        public TARGET remap(TARGET from, Object dependency) {
            return from;
        }
    }

    final class Duo<NODE, EXTR, REMAP, FINAL extends Collection<REMAP>>
            extends AbstractArrayBind<NODE, EXTR, Object, REMAP, FINAL> {
        private final Function<EXTR, REMAP> remapper;

        protected Duo(
                @Nullable GroupBind group,
                String name,
                BiFunction<UniArrayNode, String, Collection<EXTR>> extractor,
                Function<Span<REMAP>, FINAL> collectionFinalizer,
                Function<EXTR, REMAP> remapper
        ) {
            super(group, name, extractor, collectionFinalizer);

            this.remapper = remapper;
        }

        @Override
        public REMAP remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    final class Dep<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
            extends AbstractArrayBind<NODE, EXTR, DPND, REMAP, FINAL> {
        private final BiFunction<EXTR, DPND, REMAP> remapper;

        protected Dep(
                @Nullable GroupBind group,
                String name,
                BiFunction<UniArrayNode, String, Collection<EXTR>> extractor,
                Function<Span<REMAP>, FINAL> collectionFinalizer,
                BiFunction<EXTR, DPND, REMAP> remapper
        ) {
            super(group, name, extractor, collectionFinalizer);

            this.remapper = remapper;
        }

        @Override
        public REMAP remap(EXTR from, DPND dependency) {
            return remapper.apply(from,
                    Objects.requireNonNull(dependency,
                            "Dependecy object is " + "null"
                    )
            );
        }
    }
}
