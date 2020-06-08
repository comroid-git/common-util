package org.comroid.varbind.model;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.Span;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;
import org.comroid.varbind.bind.VarBind;

import java.util.function.BiFunction;

public abstract class AbstractObjectBind<EXTR, DPND, REMAP> implements VarBind<EXTR, DPND, REMAP, REMAP> {
    private final String fieldName;
    private final BiFunction<UniObjectNode, String, Span<EXTR>> extractor;
    private final GroupBind group;

    @Override
    public final String getFieldName() {
        return fieldName;
    }

    @Override
    public final GroupBind<?, DPND> getGroup() {
        return group;
    }

    protected AbstractObjectBind(
            GroupBind group, String fieldName, BiFunction<UniObjectNode, String, Span<EXTR>> extractor
    ) {
        this.fieldName = fieldName;
        this.extractor = extractor;
        this.group = group;

        group.addChild(Polyfill.uncheckedCast(this));
    }

    @Override
    public final Span<EXTR> extract(UniObjectNode node) {
        return extractor.apply(node, fieldName);
    }

    @Override
    public final REMAP finish(Span<REMAP> parts) {
        return parts.get();
    }
}
