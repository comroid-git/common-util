package org.comroid.varbind;

import java.util.Optional;
import java.util.function.BiFunction;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

import org.jetbrains.annotations.Nullable;

abstract class AbstractObjectBind<NODE, EXTR, DPND, REMAP>
        implements VarBind<NODE, EXTR, DPND, REMAP, REMAP> {
    private final String name;
    private final @Nullable GroupBind group;
    private final BiFunction<? super UniObjectNode<NODE, ?, ? super EXTR>, String, Span<EXTR>> extractor;

    protected AbstractObjectBind(
            @Nullable GroupBind group,
            String name,
            BiFunction<? super UniObjectNode<? super NODE, ?, ? super EXTR>, String, EXTR> extractor
    ) {
        this.name = name;
        this.group = group;
        this.extractor = extractor.andThen(Span::singleton);
    }

    @Override
    public Span<EXTR> extract(UniNode<NODE> node) {
        if (node.getType() == Primitive.ARRAY) {
            throw new IllegalArgumentException("VarBind cannot extract from Array Nodes");
        }

        return extractor.apply((UniObjectNode) node, name);
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
    public final REMAP finish(Span<REMAP> parts) {
        return parts.get();
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
