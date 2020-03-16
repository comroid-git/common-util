package org.comroid.varbind.bind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.uniform.data.SeriLib;

public final class GroupBind<BAS, OBJ extends BAS, ARR extends BAS> {
    private final List<? super VarBind> children = new ArrayList<>();
    private final SeriLib<BAS, OBJ, ARR> seriLib;
    private final String groupName;

    public GroupBind(SeriLib<BAS, OBJ, ARR> seriLib, String groupName) {
        this.seriLib = seriLib;
        this.groupName = groupName;
    }

    public final <T> VarBind.Uno<OBJ, T> bind1Stage(String name, BiFunction<OBJ, String, T> extractor) {
        final VarBind.Uno<OBJ, T> bind = new VarBind.Uno<>(this, name, extractor);

        children.add(bind);

        return bind;
    }

    public final <T, X> VarBind.Duo<OBJ, T, X> bind2Stage(String name, BiFunction<OBJ, String, T> extractor, Function<T, X> remapper) {
        final VarBind.Duo<OBJ, T, X> bind = new VarBind.Duo<>(this, name, extractor, remapper);

        children.add(bind);

        return bind;
    }

    public final <T, X, Y> VarBind.Dep<OBJ, T, X, Y> bindDependent(String name, BiFunction<OBJ, String, T> extractor, BiFunction<Y, T, X> resolver) {
        final VarBind.Dep<OBJ, T, X, Y> bind = new VarBind.Dep<>(this, name, extractor, resolver);

        children.add(bind);

        return bind;
    }

    public final <T, C extends Collection<T>> ArrayBind.Uno<OBJ, T, C> list1Stage(
            String name,
            Class<T> seriOut,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Uno<OBJ, T, C> bind = new ArrayBind.Uno<>(this, name, seriLib., collectionProvider);

        children.add(bind);

        return bind;
    }

    public final <T, R, C extends Collection<R>> ArrayBind.Duo<OBJ, T, R, C> list2Stage(
            String name,
            BiFunction<OBJ, String, T> extractor,
            Function<T, R> remapper,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Duo<OBJ, T, R, C> bind = new ArrayBind.Duo<>(this, name, extractor, (aVoid, it) -> remapper.apply(it), collectionProvider);

        children.add(bind);

        return bind;
    }

    public final <T, R, Y, C extends Collection<R>> ArrayBind.Dep<OBJ, T, R, Y, C> listDependent(
            String name,
            BiFunction<OBJ, String, T> extractor,
            BiFunction<Y, T, R> resolver,
            Supplier<C> collectionProvider
    ) {
        final ArrayBind.Dep<OBJ, T, R, Y, C> bind = new ArrayBind.Dep<>(this, name, extractor, resolver, collectionProvider);

        children.add(bind);

        return bind;
    }

    public final String getName() {
        return groupName;
    }

    public final List<? super VarBind> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
