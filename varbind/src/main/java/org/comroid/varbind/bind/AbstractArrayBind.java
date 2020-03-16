package org.comroid.varbind.bind;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.iter.Span;
import org.comroid.varbind.model.VariableCarrier;

import org.jetbrains.annotations.Nullable;

/**
 * {@link Collection} building Variable definition with 2 mapping Stages.
 * Used for deserializing arrays of data.
 *
 * @param <S>    The serialization input Type
 * @param <A>    The mapping output Type
 * @param <D>    The dependency Type
 * @param <C>    The output {@link Collection} type; this is what you get from {@link VariableCarrier#getVar(VarBind)}
 * @param <NODE> Serialization Library Type of the serialization Node
 *
 * @see Dep
 */
abstract class AbstractArrayBind<S, A, D, C extends Collection<A>, NODE> extends AbstractObjectBind<S, A, D, C, NODE> {
    protected static <S, C extends Collection<S>> Function<Span<S>, C> mergefuncWithProvider(Supplier<C> collectionProvider) {
        return new Function<Span<S>, C>() {
            private final Supplier<C> provider = collectionProvider;

            @Override
            public C apply(Span<S> span) {
                final C collection = provider.get();

                collection.addAll(span);

                return collection;
            }
        };
    }

    private final BiFunction<D, S, A> resolver;
    private final Function<Span<A>, C> finisher;

    protected AbstractArrayBind(
            @Nullable GroupBind group,
            String name,
            BiFunction<NODE, String, S> extractor,
            BiFunction<D, S, A> resolver,
            Function<Span<A>, C> finisher
    ) {
        super(group, name, extractor.andThen(Span::fixedSize));

        this.resolver = resolver;
        this.finisher = finisher;
    }

    @Override
    public A remap(S from, D dependency) {
        return resolver.apply(Objects.requireNonNull(dependency, "Dependency Object is null"), from);
    }

    @Override
    public final C finish(Span<A> parts) {
        return finisher.apply(parts);
    }
}
