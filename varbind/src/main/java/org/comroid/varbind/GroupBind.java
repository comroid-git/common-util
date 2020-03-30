package org.comroid.varbind;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.SeriLib;
import org.comroid.varbind.model.VariableCarrier;

import static org.comroid.common.Polyfill.deadCast;

public final class GroupBind<BAS, OBJ extends BAS, ARR extends BAS> {
    public final String getName() {
        return groupName;
    }

    public final List<? extends VarBind<?, ?, ?, ?, OBJ>> getChildren() {
        return Collections.unmodifiableList(children);
    }
    private final List<? extends VarBind<?, ?, ?, ?, OBJ>> children = new ArrayList<>();
    private final SeriLib<BAS, OBJ, ARR>                   seriLib;
    private final String                                   groupName;

    public GroupBind(SeriLib<BAS, OBJ, ARR> seriLib, String groupName) {
        this.seriLib   = seriLib;
        this.groupName = groupName;
    }

    public <R extends VariableCarrier<?, ?, D>, D> BiFunction<D, OBJ, R> autoRemapper(
            Class<R> resultType,
            Class<D> dependencyType
    ) {
        final Class<?>[]                  typesUnordered   = {
                SeriLib.class,
                seriLib.objectType.typeClass(),
                dependencyType
        };
        final Optional<Constructor<R>> optConstructor = ReflectionHelper.findConstructor(
                resultType, typesUnordered);

        if (!optConstructor.isPresent()) throw new NoSuchElementException(
                "Could not find any fitting constructor");

        class Local implements BiFunction<D, OBJ, R> {
            private final Constructor<R> constr;

            public Local(Constructor<R> constr) {
                this.constr = constr;
            }

            @Override
            public R apply(D dependencyObject, OBJ obj) {
                return ReflectionHelper.instance(
                        constr, ReflectionHelper.arrange(
                                new Object[]{ seriLib, obj, dependencyObject }, constr.getParameterTypes()));
            }
        }

        return new Local(optConstructor.get());
    }

    public final <T> VarBind.Uno<OBJ, T> bind1Stage(
            String name, Class<T> extractTarget
    ) {
        return bind1Stage(name, extractor(extractTarget));
    }

    private <T> BiFunction<OBJ, String, T> extractor(final Class<T> extractTarget) {
        return (node, fieldName) -> seriLib.dummy(node)
                                           .getValueAs(fieldName, extractTarget);
    }

    public final <T> VarBind.Uno<OBJ, T> bind1Stage(
            String name, BiFunction<OBJ, String, T> extractor
    ) {
        final VarBind.Uno<OBJ, T> bind = new VarBind.Uno<>(seriLib, this, name, extractor);

        children.add(deadCast(bind));

        return bind;
    }

    public final <T, X> VarBind.Duo<OBJ, T, X> bind2Stage(
            String name, Class<T> extractTarget, Function<T, X> remapper
    ) {
        return bind2Stage(name, extractor(extractTarget), remapper);
    }

    public final <T, X> VarBind.Duo<OBJ, T, X> bind2Stage(
            String name, BiFunction<OBJ, String, T> extractor, Function<T, X> remapper
    ) {
        final VarBind.Duo<OBJ, T, X> bind = new VarBind.Duo<>(
                seriLib, this, name, extractor, remapper);

        children.add(deadCast(bind));

        return bind;
    }

    public final <T, X, Y> VarBind.Dep<OBJ, T, X, Y> bindDependent(
            String name, Class<T> extractTarget, BiFunction<Y, T, X> resolver
    ) {
        return bindDependent(name, extractor(extractTarget), resolver);
    }

    public final <T, X, Y> VarBind.Dep<OBJ, T, X, Y> bindDependent(
            String name, BiFunction<OBJ, String, T> extractor, BiFunction<Y, T, X> resolver
    ) {
        final VarBind.Dep<OBJ, T, X, Y> bind = new VarBind.Dep<>(
                seriLib, this, name, extractor, resolver);

        children.add(deadCast(bind));

        return bind;
    }

    public final <T, C extends Collection<T>> ArrayBind.Uno<OBJ, T, C> list1Stage(
            String name, Class<T> extractTarget, Supplier<C> collectionProvider
    ) {
        return list1Stage(name, typeExtractor(extractTarget), collectionProvider);
    }

    private <T> Function<OBJ, T> typeExtractor(final Class<T> target) {
        return node -> seriLib.dummy(node)
                              .getValueAs(null, target);
    }

    public final <T, C extends Collection<T>> ArrayBind.Uno<OBJ, T, C> list1Stage(
            String name, Function<OBJ, T> dataExtractor, Supplier<C> collectionProvider
    ) {
        final ArrayBind.Uno<OBJ, T, C> bind = new ArrayBind.Uno<>(seriLib, this, name,
                                                                  seriLib.arrayExtractor,
                                                                  dataExtractor, collectionProvider
        );

        children.add(deadCast(bind));

        return bind;
    }

    public final <T, R, C extends Collection<R>> ArrayBind.Duo<OBJ, T, R, C> list2Stage(
            String name,
            Class<T> extractTarget,
            Function<T, R> remapper,
            Supplier<C> collectionProvider
    ) {
        return list2Stage(name, typeExtractor(extractTarget), remapper, collectionProvider);
    }

    public final <T, R, C extends Collection<R>> ArrayBind.Duo<OBJ, T, R, C> list2Stage(
            String name,
            Function<OBJ, T> dataExtractor,
            Function<T, R> remapper,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Duo<OBJ, T, R, C> bind = new ArrayBind.Duo<>(seriLib, this, name,
                                                                     seriLib.arrayExtractor,
                                                                     dataExtractor,
                                                                     (aVoid, it) -> remapper.apply(
                                                                             it), collectionProvider
        );

        children.add(deadCast(bind));

        return bind;
    }

    public final <T, R, Y, C extends Collection<R>> ArrayBind.Dep<OBJ, T, R, Y, C> listDependent(
            String name,
            Class<T> extractTarget,
            BiFunction<Y, T, R> resolver,
            Supplier<C> collectionProvider
    ) {
        return listDependent(name, typeExtractor(extractTarget), resolver, collectionProvider);
    }

    public final <T, R, Y, C extends Collection<R>> ArrayBind.Dep<OBJ, T, R, Y, C> listDependent(
            String name,
            Function<OBJ, T> dataExtractor,
            BiFunction<Y, T, R> resolver,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Dep<OBJ, T, R, Y, C> bind = new ArrayBind.Dep<>(seriLib, this, name,
                                                                        seriLib.arrayExtractor,
                                                                        dataExtractor, resolver,
                                                                        collectionProvider
        );

        children.add(deadCast(bind));

        return bind;
    }
}
