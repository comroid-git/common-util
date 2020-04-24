package org.comroid.varbind;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniObjectNode;

public interface ReBind<EXTR, DPND, REMAP> extends VarBind<EXTR, DPND, REMAP, REMAP> {
    final class Duo<EXTR, FINAL> extends AbstractReBind<EXTR, Object, FINAL> {
        private final Function<EXTR, FINAL> remapper;

        public Duo(VarBind<?, Object, ?, EXTR> underlying, GroupBind group, Function<EXTR, FINAL> remapper) {
            super(underlying, group);

            this.remapper = remapper;
        }

        @Override
        public FINAL remap(EXTR from, Object dependency) {
            return remapper.apply(from);
        }
    }

    final class Dep<EXTR, DPND, FINAL> extends AbstractReBind<EXTR, DPND, FINAL> {
        private final BiFunction<EXTR, DPND, FINAL> remapper;

        public Dep(
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

    VarBind<?, DPND, ?, EXTR> getUnderlying();

    @Override
    default String getFieldName() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    default Span<EXTR> extract(UniObjectNode node) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    default REMAP finish(Span<REMAP> parts) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    REMAP remap(EXTR from, DPND dependency);

    @Override
    GroupBind getGroup();
}
