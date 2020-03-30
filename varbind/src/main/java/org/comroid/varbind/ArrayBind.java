package org.comroid.varbind;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public interface ArrayBind<S, A, D, C extends Collection<A>, OBJ> extends VarBind<S, A, D, C, OBJ> {
    final class Uno<NODE, S, C extends Collection<S>>
            extends AbstractArrayBind<S, S, Object, C, NODE> {
        Uno(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, ?> arrayExtractor,
                Function<NODE, S> dataExtractor,
                Supplier<C> collectionProvider
        ) {
            super(seriLib,
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

    final class Duo<NODE, S, A, C extends Collection<A>>
            extends AbstractArrayBind<S, A, Object, C, NODE> {
        Duo(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, ?> arrayExtractor,
                Function<NODE, S> dataExtractor,
                BiFunction<Object, S, A> resolver,
                Supplier<C> collectionProvider
        ) {
            super(seriLib,
                  group,
                  name,
                  arrayExtractor,
                  dataExtractor,
                  resolver,
                  mergefuncWithProvider(collectionProvider)
            );
        }
    }

    final class Dep<NODE, S, A, D, C extends Collection<A>>
            extends AbstractArrayBind<S, A, D, C, NODE> {
        Dep(
                Object seriLib,
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, ?> arrayExtractor,
                Function<NODE, S> dataExtractor,
                BiFunction<D, S, A> resolver,
                Supplier<C> collectionProvider
        ) {
            super(seriLib,
                  group,
                  name,
                  arrayExtractor,
                  dataExtractor,
                  resolver,
                  mergefuncWithProvider(collectionProvider)
            );
        }
    }
}
