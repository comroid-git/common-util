package org.comroid.varbind;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.iter.Span;
import org.comroid.varbind.model.VariableCarrier;

import org.jetbrains.annotations.Nullable;

import static org.comroid.common.Polyfill.deadCast;

/**
 * {@link Collection} building Variable definition with 2 mapping Stages. Used for deserializing
 * arrays of data.
 *
 * @param <S>   The serialization input Type
 * @param <A>   The mapping output Type
 * @param <D>   The dependency Type
 * @param <C>   The output {@link Collection} type; this is what you get from {@link
 *              VariableCarrier#getVar(VarBind)}
 * @param <OBJ> Serialization Library Type of the serialization Node
 */
abstract class AbstractArrayBind<S, A, D, C extends Collection<A>, OBJ>
        extends AbstractObjectBind<S, A, D, C, OBJ> implements ArrayBind {
    protected static <S, C extends Collection<S>> Function<Span<S>, C> mergefuncWithProvider(
            Supplier<C> collectionProvider
    ) {
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

    private final BiFunction<D, S, A>        resolver;
    private final Function<Span<A>, C>       finisher;
    private final BiFunction<OBJ, String, ?> arrayExtractor;
    private final Function<OBJ, S>           dataExtractor;

    protected AbstractArrayBind(
            Object seriLib,
            @Nullable GroupBind group,
            String name,
            BiFunction<OBJ, String, ?> arrayExtractor,
            Function<OBJ, S> dataExtractor,
            BiFunction<D, S, A> resolver,
            Function<Span<A>, C> finisher
    ) {
        super(seriLib, group, name, null);

        this.arrayExtractor = arrayExtractor;
        this.dataExtractor  = dataExtractor;
        this.resolver       = resolver;
        this.finisher       = finisher;
    }

    @Override
    public Span<S> extract(OBJ obj) {
        final Object array = arrayExtractor.apply(obj, getName());

        return seriLib().arrayType.split(deadCast(array))
                                  .stream()
                                  .map(it -> (OBJ) it)
                                  .map(dataExtractor)
                                  .collect(Span.<S>make().fixedSize(true)
                                                         .collector());
    }

    @Override
    public String toString() {
        return String.format("ArrayBind@%s", getPath());
    }

    @Override
    public A remap(S from, D dependency) {
        return resolver.apply(
                Objects.requireNonNull(dependency, "Dependency Object is null"),
                from
        );
    }

    @Override
    public final C finish(Span<A> parts) {
        return finisher.apply(parts);
    }
}
