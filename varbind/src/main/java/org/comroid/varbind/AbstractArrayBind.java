package org.comroid.varbind;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;

import org.jetbrains.annotations.Nullable;

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
abstract class AbstractArrayBind<NODE, EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
        implements ArrayBind<NODE, EXTR, DPND, REMAP, FINAL> {
    private final String name;
    private final @Nullable GroupBind group;
    private final BiFunction<UniArrayNode, String, Collection<EXTR>> extractor;
    private final Function<Span<REMAP>, FINAL> collectionFinalizer;

    protected AbstractArrayBind(
            @Nullable GroupBind group,
            String name,
            BiFunction<UniArrayNode, String, Collection<EXTR>> extractor,
            Function<Span<REMAP>, FINAL> collectionFinalizer
    ) {
        this.name = name;
        this.group = group;
        this.extractor = extractor;
        this.collectionFinalizer = collectionFinalizer;
    }

    @Override
    public Span<EXTR> extract(UniNode node) {
        return Span.immutable(extractor.apply(node.asArrayNode(), name));
    }

    @Override
    public String toString() {
        return String.format("VarBind@%s", getPath());
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public abstract REMAP remap(EXTR from, DPND dependency);

    @Override
    public final FINAL finish(Span<REMAP> parts) {
        return collectionFinalizer.apply(parts);
    }

    public final String getPath() {
        return getGroup().map(groupBind -> groupBind.getName() + ".")
                .orElse("") + name;
    }

    @Override
    public final Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }
}
