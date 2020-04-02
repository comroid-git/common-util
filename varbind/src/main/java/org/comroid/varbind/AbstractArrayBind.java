package org.comroid.varbind;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;

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
    private final           String                                                                            name;
    private final @Nullable GroupBind                                                                         group;
    private final           BiFunction<? super UniArrayNode<NODE, ?, ? super EXTR>, String, Collection<EXTR>> extractor;
    private final           Function<Span<REMAP>, FINAL>                                                      collectionFinalizer;

    protected AbstractArrayBind(
            @Nullable GroupBind group,
            String name,
            BiFunction<? super UniArrayNode<NODE, ?, ? super EXTR>, String, Collection<EXTR>> extractor,
            Function<Span<REMAP>, FINAL> collectionFinalizer
    ) {
        this.name                = name;
        this.group               = group;
        this.extractor           = extractor;
        this.collectionFinalizer = collectionFinalizer;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public Span<EXTR> extract(UniNode<NODE> node) {
        if (node.getType() == Primitive.OBJECT) {
            throw new IllegalArgumentException("ArrayBind cannot extract from Object Nodes");
        }

        return Span.immutable(extractor.apply((UniArrayNode) node, name));
    }

    @Override
    public abstract REMAP remap(EXTR from, DPND dependency);

    @Override
    public final FINAL finish(Span<REMAP> parts) {
        return collectionFinalizer.apply(parts);
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
    public final Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }
}
