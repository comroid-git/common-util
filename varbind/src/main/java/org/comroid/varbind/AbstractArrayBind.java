package org.comroid.varbind;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.iter.Span;

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
abstract class AbstractArrayBind<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>implements ArrayBind<NODE, EXTR, DPND, REMAP, FINAL> {
    private final           Object                            seriLib;
    private final           String                            name;
    private final @Nullable GroupBind                         group;
    private final           BiFunction<NODE, String, Span<EXTR>> extractor;

    protected AbstractObjectBind(
            Object seriLib,
            @Nullable GroupBind group,
            String name,
            BiFunction<NODE, String, Span<EXTR>> extractor
    ) {
        this.seriLib   = seriLib;
        this.name      = name;
        this.group     = group;
        this.extractor = extractor;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public Span<EXTR> extract(NODE NODE) {
        return extractor.apply(NODE, name);
    }

    @Override
    public String toString() {
        return String.format("VarBind@%s", getPath());
    }

    public final String getPath() {
        return getGroup().map(groupBind -> groupBind.getName() + ".")
                         .orElse("") + name;
    }

    @Override
    public final REMAP finish(Span<REMAP> parts) {
        return parts.get();
    }

    @Override
    public final Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }

    protected <BAS, OBJ extends BAS, ARR extends BAS> SeriLib<BAS, OBJ, ARR> seriLib() {
        return Polyfill.deadCast(seriLib);
    }
}
