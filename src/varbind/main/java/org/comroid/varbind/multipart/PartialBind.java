package org.comroid.varbind.multipart;

import org.comroid.common.iter.Span;
import org.comroid.common.ref.Specifiable;
import org.comroid.spellbind.annotation.Partial;
import org.comroid.spellbind.model.TypeFragment;
import org.comroid.uniform.node.UniNode;
import org.comroid.varbind.bind.GroupBind;

public final class PartialBind {
    public interface BindFragment extends TypeFragment<BindFragment> {
    }

    @Partial
    public interface Base extends BindFragment {
        String getFieldName();

        boolean isRequired();
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
        FINAL finish(Span<REMAP> parts);

        boolean isListing();
    }
}
