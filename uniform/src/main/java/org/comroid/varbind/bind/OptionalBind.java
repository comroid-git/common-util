package org.comroid.varbind.bind;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.Span;
import org.comroid.varbind.model.Reprocessed;

import java.util.Optional;

public interface OptionalBind<T> extends Reprocessed.Underlying<VarBind<Object, Object, Object, T>, Object, Object, Object, T, Optional<T>> {
    static <T> OptionalBind<T> ofBind(VarBind<?, ?, ?, T> bind) {
        if (bind instanceof OptionalBind)
            return Polyfill.uncheckedCast(bind);

        return new Support.OfBind<>(Polyfill.uncheckedCast(bind));
    }

    @Override
    default Optional<T> finish(Span<T> parts) {
        return parts.wrap();
    }

    final class Support {
        private static final class OfBind<T> implements OptionalBind<T> {
            private final VarBind<Object, Object, Object, T> underlying;

            private OfBind(VarBind<Object, Object, Object, T> underlying) {
                this.underlying = underlying;
            }

            @Override
            public VarBind<Object, Object, Object, T> getUnderlying() {
                return underlying;
            }
        }
    }
}
