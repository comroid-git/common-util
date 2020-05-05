package org.comroid.varbind.model;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import org.comroid.common.Polyfill;
import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.ArrayBind;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;
import org.comroid.varbind.container.DataContainerBase;

/**
 * {@link Collection} building Variable definition with 2 mapping Stages. Used for deserializing arrays of data.
 *
 * @param <S>   The serialization input Type
 * @param <A>   The mapping output Type
 * @param <D>   The dependency Type
 * @param <C>   The output {@link Collection} type; this is what you get from {@link DataContainerBase#getVar(VarBind)}
 * @param <OBJ> Serialization Library Type of the serialization Node
 */
abstract class AbstractArrayBind<EXTR, DPND, REMAP, FINAL extends Collection<REMAP>>
        implements ArrayBind<EXTR, DPND, REMAP, FINAL> {
    final         Supplier<FINAL>                   collectionSupplier;
    private final String                            fieldName;
    private final Function<? extends UniNode, EXTR> extractor;
    private final GroupBind group;

    protected AbstractArrayBind(
            GroupBind group, String fieldName, Function<? extends UniNode, EXTR> extractor, Supplier<FINAL> collectionSupplier
    ) {
        this.fieldName          = fieldName;
        this.extractor          = extractor;
        this.collectionSupplier = collectionSupplier;
        this.group              = group;

        group.children.add(Polyfill.uncheckedCast(this));
    }

    @Override
    public final String getFieldName() {
        return fieldName;
    }

    @Override
    public final Span<EXTR> extract(UniObjectNode node) {
        return node.get(fieldName)
                .asArrayNode()
                .asNodeList()
                .stream()
                .map(arrayMember -> extractor.apply(Polyfill.uncheckedCast(arrayMember)))
                .collect(Span.collector());
    }

    @Override
    public final FINAL finish(Span<REMAP> parts) {
        final FINAL yields = collectionSupplier.get();
        yields.addAll(parts);

        return yields;
    }

    @Override
    public final GroupBind<?, DPND> getGroup() {
        return group;
    }
}
