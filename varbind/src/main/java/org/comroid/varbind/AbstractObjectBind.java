package org.comroid.varbind;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.BiFunction;

import org.comroid.common.iter.Span;
import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import org.jetbrains.annotations.Nullable;

abstract class AbstractObjectBind<NODE, EXTR, DPND, REMAP>
        implements VarBind<NODE, EXTR, DPND, REMAP, REMAP> {
    private final String name;
    private final @Nullable GroupBind group;
    private final BiFunction<UniObjectNode, String, Span<EXTR>> extractor;

    protected AbstractObjectBind(
            @Nullable GroupBind group,
            String name,
            BiFunction<UniObjectNode, String, EXTR> extractor
    ) {
        this.name = name;
        this.group = group;
        this.extractor = extractor.andThen(Span::singleton);
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
