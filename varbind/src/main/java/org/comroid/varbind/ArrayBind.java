package org.comroid.varbind;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.iter.Span;
import org.comroid.common.iter.Span.ModifyPolicy;

import org.jetbrains.annotations.Nullable;

public interface ArrayBind<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
        extends VarBind<NODE, EXTR, DPND, REMAP, FINAL> {
    final class Uno<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
            extends AbstractArrayBind<NODE, EXTR, DPND, REMAP, FINAL> {
        Uno(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, ?> arrayExtractor,
                Function<NODE, S> dataExtractor,
                Supplier<C> collectionProvider
        ) {
            super(
                    seriLib,
                    group,
                    name,
                    arrayExtractor,
                    dataExtractor,
                    null,
                    mergefuncWithProvider(collectionProvider)
            );
        }

        @Override
        public final S remap(S from, Object dependency) {
            return from;
        }
    }

    final class Duo<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
            extends AbstractArrayBind<NODE, EXTR, DPND, REMAP, FINAL> {
        Duo(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, ?> arrayExtractor,
                Function<NODE, S> dataExtractor,
                BiFunction<Object, S, A> resolver,
                Supplier<C> collectionProvider
        ) {
            super(
                    seriLib,
                    group,
                    name,
                    arrayExtractor,
                    dataExtractor,
                    resolver,
                    mergefuncWithProvider(collectionProvider)
            );
        }
    }

    final class Dep<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
            extends AbstractArrayBind<NODE, EXTR, DPND, REMAP, FINAL> {
        Dep(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, ?> arrayExtractor,
                Function<NODE, S> dataExtractor,
                BiFunction<D, S, A> resolver,
                Supplier<C> collectionProvider
        ) {
            super(
                    seriLib,
                    group,
                    name,
                    arrayExtractor,
                    dataExtractor,
                    resolver,
                    mergefuncWithProvider(collectionProvider)
            );
        }
    }

    @Override
    String getName();

    @Override
    Span<EXTR> extract(NODE node);

    @Override
    REMAP remap(EXTR from, DPND dependency);

    @Override
    default FINAL finish(Span<REMAP> parts) {
        return (FINAL) parts.reconfigure()
                            .fixedSize(true)
                            .nullPolicy(ModifyPolicy.UNMODIFIABLE)
                            .span();
    }
}
