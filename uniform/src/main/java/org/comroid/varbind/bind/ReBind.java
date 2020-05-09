package org.comroid.varbind.bind;

import org.comroid.common.iter.Span;
import org.comroid.varbind.model.AbstractReBind;
import org.comroid.varbind.model.Reprocessed;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ReBind<EXTR, DPND, REMAP> extends Reprocessed<EXTR, DPND, REMAP, REMAP> {
    @Override
    default String getFieldName() {
        return getUnderlying().getFieldName();
    }

    VarBind<?, DPND, ?, EXTR> getUnderlying();

    final class TwoStage<EXTR, FINAL> extends AbstractReBind<EXTR, Object, FINAL> {
        private final Function<EXTR, FINAL> remapper;

        public TwoStage(VarBind<?, Object, ?, EXTR> underlying, GroupBind group, Function<EXTR, FINAL> remapper) {
            super(underlying, group);

            this.remapper = remapper;
        }

        @Override
        public FINAL remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    final class DependentTwoStage<EXTR, DPND, FINAL> extends AbstractReBind<EXTR, DPND, FINAL> {
        private final BiFunction<EXTR, DPND, FINAL> remapper;

        public DependentTwoStage(
                VarBind<?, DPND, ?, EXTR> underlying, GroupBind group, BiFunction<EXTR, DPND, FINAL> remapper
        ) {
            super(underlying, group);

            this.remapper = remapper;
        }

        @Override
        public FINAL remap(EXTR from, DPND dependency) {
            return remapper.apply(from, Objects.requireNonNull(dependency, "Dependency object is null"));
        }
    }
}
