package org.comroid.varbind.bind;

import org.comroid.common.iter.Span;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.multipart.PartialBind;

public interface VarBind<EXTR, DPND, REMAP, FINAL> extends
        PartialBind.Base,
        PartialBind.Grouped<DPND>,
        PartialBind.Extractor<EXTR>,
        PartialBind.Remapper<EXTR, DPND, REMAP>,
        PartialBind.Finisher<REMAP, FINAL> {
    default FINAL getFrom(UniObjectNode node) {
        return getFrom(null, node);
    }

    default FINAL getFrom(DPND dependencyObject, UniObjectNode node) {
        return process(dependencyObject, extract(node));
    }

    default Span<REMAP> remapAll(final DPND dependency, Span<EXTR> from) {
        return from.pipe()
                .map(each -> remap(each, dependency))
                .span();
    }

    default FINAL process(final DPND dependency, Span<EXTR> from) {
        return finish(remapAll(dependency, from));
    }
}
