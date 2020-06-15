package org.comroid.varbind.multipart;

import org.comroid.api.Polyfill;
import org.comroid.api.Specifiable;
import org.comroid.mutatio.span.Span;
import org.comroid.spellbind.annotation.Partial;
import org.comroid.spellbind.model.TypeFragment;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.bind.GroupBind;

public final class PartialBind {
    public interface BindFragment extends TypeFragment<BindFragment>, Specifiable<BindFragment> {
    }

    @Partial
    public interface Base<EXTR, DPND, REMAP, FINAL> extends BindFragment {
        String getFieldName();

        boolean isRequired();

        default FINAL getFrom(UniObjectNode node) {
            return getFrom(null, node);
        }

        default FINAL getFrom(DPND dependencyObject, UniObjectNode node) {
            return process(dependencyObject, as(Extractor.class)
                    .map(Polyfill::<Extractor<EXTR>>uncheckedCast)
                    .orElseThrow(AssertionError::new)
                    .extract(node));
        }

        default Span<REMAP> remapAll(final DPND dependency, Span<EXTR> from) {
            return from.pipe()
                    .map(each -> as(Remapper.class)
                            .map(Polyfill::<Remapper<EXTR, DPND, REMAP>>uncheckedCast)
                            .orElseThrow(AssertionError::new)
                            .remap(each, dependency))
                    .span();
        }

        default FINAL process(final DPND dependency, Span<EXTR> from) {
            return as(Finisher.class)
                    .map(Polyfill::<Finisher<REMAP, FINAL>>uncheckedCast)
                    .orElseThrow(AssertionError::new)
                    .finish(remapAll(dependency, from));
        }
    }

    @Partial
    public interface Grouped<DPND> extends BindFragment {
        GroupBind<?, DPND> getGroup();
    }

    @Partial
    public interface Extractor<EXTR> extends BindFragment {
        Span<EXTR> extract(UniNode from);
    }

    @Partial
    public interface Remapper<EXTR, DPND, REMAP> extends BindFragment {
        REMAP remap(EXTR from, DPND dependency);
    }

    @Partial
    public interface Finisher<REMAP, FINAL> extends BindFragment {
        boolean isListing();

        FINAL finish(Span<REMAP> parts);
    }
}
