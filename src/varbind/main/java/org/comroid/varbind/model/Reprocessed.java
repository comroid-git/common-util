package org.comroid.varbind.model;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.Span;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;

public interface Reprocessed<EXTR, DPND, REMAP, FINAL> extends VarBind<EXTR, DPND, REMAP, FINAL> {
    @Override
    default Span<EXTR> extract(UniObjectNode node) {
        return Span.empty();
    }

    @Override
    default FINAL finish(Span<REMAP> parts) {
        return Polyfill.uncheckedCast(parts.get());
    }

    interface Underlying<V extends VarBind<EXTR, DPND, REMAP, I>, EXTR, DPND, REMAP, I, T> extends Reprocessed<EXTR, DPND, I, T> {
        V getUnderlying();

        @Override
        default String getFieldName() {
            return getUnderlying().getFieldName();
        }

        @Override
        default GroupBind<?, DPND> getGroup() {
            return getUnderlying().getGroup();
        }

        @Override
        default I remap(EXTR from, DPND dependency) {
            return getUnderlying()
                    .finish(Span.singleton(getUnderlying()
                            .remap(from, dependency)));
        }

        class Base<EXTR, DPND, REMAP, FINAL> implements Underlying<VarBind<EXTR, DPND, REMAP, FINAL>, EXTR, DPND, REMAP, FINAL, FINAL> {
            private final VarBind<EXTR, DPND, REMAP, FINAL> underlying;

            @Override
            public VarBind<EXTR, DPND, REMAP, FINAL> getUnderlying() {
                return underlying;
            }

            public Base(VarBind<EXTR, DPND, REMAP, FINAL> underlying) {
                this.underlying = underlying;
            }
        }
    }
}
