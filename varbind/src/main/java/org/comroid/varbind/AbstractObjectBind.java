package org.comroid.varbind;

import java.util.function.BiFunction;

import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

abstract class AbstractObjectBind<EXTR, DPND, REMAP> implements VarBind<EXTR, DPND, REMAP, REMAP> {
    private final String fieldName;
    private final BiFunction<UniObjectNode, String, Span<EXTR>> extractor;
    private final GroupBind group;

    protected AbstractObjectBind(GroupBind group, String fieldName, BiFunction<UniObjectNode, String, Span<EXTR>> extractor) {
        this.fieldName = fieldName;
        this.extractor = extractor;
        this.group = group;
    }

    @Override
    public final String getFieldName() {
        return fieldName;
    }

    @Override
    public final Span<EXTR> extract(UniObjectNode node) {
        return extractor.apply(node, fieldName);
    }

    @Override
    public final REMAP finish(Span<REMAP> parts) {
        return parts.get();
    }

    @Override
    public final GroupBind getGroup() {
        return group;
    }
}
