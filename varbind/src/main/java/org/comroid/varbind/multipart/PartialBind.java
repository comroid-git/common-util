package org.comroid.varbind.multipart;

import org.comroid.common.iter.Span;
import org.comroid.common.ref.Specifiable;
import org.comroid.spellbind.model.TypeFragment;
import org.comroid.uniform.node.UniNode;
import org.comroid.varbind.bind.GroupBind;

public final class PartialBind {
    public interface BindFragment extends TypeFragment<BindFragment> {
    }

    public interface Base extends BindFragment {
        String getFieldName();

        boolean isRequired();
    }

    public interface Grouped<DPND> extends BindFragment {
        GroupBind<?, DPND> getGroup();
    }

    public interface Extractor<EXTR> extends BindFragment {
        Span<EXTR> extract(UniNode from);
    }

    public interface Remapper<EXTR, DPND, REMAP> extends BindFragment {
        REMAP remap(EXTR from, DPND dependency);
    }

    public interface Finisher<REMAP, FINAL> extends BindFragment {
        FINAL finish(Span<REMAP> parts);

        boolean isListing();
    }
}
