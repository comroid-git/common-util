package org.comroid.varbind.bind;

import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;

public final class PartialBind {
    public interface Grouped<DPND> {
        GroupBind<?, DPND> getGroup();
    }

    public interface Extractor<EXTR> {
        Span<EXTR> extract(UniObjectNode from);
    }

    public interface Remapper<EXTR, DPND, REMAP> {
        REMAP remap(EXTR from, DPND dependency);
    }

    public interface Finisher<REMAP, FINAL> {
        FINAL finish(Span<REMAP> parts);
    }
}
