package org.comroid.varbind;

import java.lang.reflect.Constructor;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.Polyfill;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.SeriLib;
import org.comroid.varbind.model.VariableCarrier;

public final class GroupBind<BAS, OBJ extends BAS, ARR extends BAS> {
    private final List<? super VarBind> children = new ArrayList<>();
    private final SeriLib<BAS, OBJ, ARR> seriLib;
    private final String groupName;

    public GroupBind(SeriLib<BAS, OBJ, ARR> seriLib, String groupName) {
        this.seriLib = seriLib;
        this.groupName = groupName;
    }

    public <R extends VariableCarrier, D> BiFunction<D, OBJ, R> autoRemapper(Class<R> resultType, Class<D> dependencyType) {
        final Class[] typesOrdered = {SeriLib.class, seriLib.objectType.typeClass(), dependencyType};
        final Optional<Constructor<R>> optConstructor = ReflectionHelper.findConstructor(resultType, typesOrdered);

        if (!optConstructor.isPresent())
            throw new NoSuchElementException("Could not find any fitting constructor");

        class Local implements BiFunction<D, OBJ, R> {
            private final Constructor<R> constr;

            public Local(Constructor<R> constr) {
                this.constr = constr;
            }

            @Override
            public R apply(D dependencyObject, OBJ obj) {
                return ReflectionHelper.instance(constr, ReflectionHelper.arrange(new Object[]{seriLib, obj, dependencyObject}, typesOrdered));
            }
        }

        return new Local(optConstructor.get());
    }

    private <T> BiFunction<OBJ, String, T> extractor(Class<T> extractTarget) {
        return (node, fieldName) -> seriLib.dummy(node).getValueAs(fieldName, extractTarget);
    }

    public final <T> VarBind.Uno<OBJ, T> bind1Stage(
            String name,
            Class<T> extractTarget
    ) {
        final VarBind.Uno<OBJ, T> bind = new VarBind.Uno<>(this, name, extractor(extractTarget));

        children.add(bind);

        return bind;
    }

    public final <T, X> VarBind.Duo<OBJ, T, X> bind2Stage(
            String name,
            Class<T> extractTarget,
            Function<T, X> remapper
    ) {
        final VarBind.Duo<OBJ, T, X> bind = new VarBind.Duo<>(this, name, extractor(extractTarget), remapper);

        children.add(bind);

        return bind;
    }

    public final <T, X, Y> VarBind.Dep<OBJ, T, X, Y> bindDependent(
            String name,
            Class<T> extractTarget,
            BiFunction<Y, T, X> resolver
    ) {
        final VarBind.Dep<OBJ, T, X, Y> bind = new VarBind.Dep<>(this, name, extractor(extractTarget), resolver);

        children.add(bind);

        return bind;
    }

    public final <T, C extends Collection<T>> ArrayBind.Uno<OBJ, T, C> list1Stage(
            String name,
            Class<T> extractTarget,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Uno<OBJ, T, C> bind = new ArrayBind.Uno<>(this, name, extractor(extractTarget), collectionProvider);

        children.add(bind);

        return bind;
    }

    public final <T, R, C extends Collection<R>> ArrayBind.Duo<OBJ, T, R, C> list2Stage(
            String name,
            Class<T> extractTarget,
            Function<T, R> remapper,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Duo<OBJ, T, R, C> bind = new ArrayBind.Duo<>(this, name, extractor(extractTarget), (aVoid, it) -> remapper.apply(it), collectionProvider);

        children.add(bind);

        return bind;
    }

    public final <T, R, Y, C extends Collection<R>> ArrayBind.Dep<OBJ, T, R, Y, C> listDependent(
            String name,
            Class<T> extractTarget,
            BiFunction<Y, T, R> resolver,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Dep<OBJ, T, R, Y, C> bind = new ArrayBind.Dep<>(this, name,
                (node, fieldName) -> seriLib.dummy(node).getValueAs(fieldName, extractTarget), resolver, collectionProvider);

        children.add(bind);

        return bind;
    }

    public final String getName() {
        return groupName;
    }

    public final List<? extends VarBind<?, ?, ?, ?, OBJ>> getChildren() {
        return (List<? extends VarBind<?, ?, ?, ?, OBJ>>) Collections.unmodifiableList(children);
    }
}
