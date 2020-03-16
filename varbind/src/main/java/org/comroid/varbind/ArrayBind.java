package org.comroid.varbind;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public interface ArrayBind {
    final class Uno<NODE, S, C extends Collection<S>> extends AbstractArrayBind<S, S, Void, C, NODE> {
        Uno(
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, S> extractor,
                Supplier<C> collectionProvider
        ) {
            super(group, name, extractor, null, mergefuncWithProvider(collectionProvider));
        }

        @Override
        public final S remap(S from, Void dependency) {
            return from;
        }
    }

    final class Duo<NODE, S, A, C extends Collection<A>> extends AbstractArrayBind<S, A, Void, C, NODE> {
        Duo(
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, S> extractor,
                BiFunction<Void, S, A> resolver,
                Supplier<C> collectionProvider
        ) {
            super(group, name, extractor, resolver, mergefuncWithProvider(collectionProvider));
        }
    }

    final class Dep<NODE, S, A, D, C extends Collection<A>> extends AbstractArrayBind<S, A, D, C, NODE> {
        Dep(
                @Nullable GroupBind group,
                String name,
                BiFunction<NODE, String, S> extractor,
                BiFunction<D, S, A> resolver,
                Supplier<C> collectionProvider
        ) {
            super(group, name, extractor, resolver, mergefuncWithProvider(collectionProvider));
        }
    }
}
