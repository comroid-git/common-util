package org.comroid.varbind.bind;

import org.comroid.varbind.multipart.PartialBind;

public interface VarBind<EXTR, DPND, REMAP, FINAL> extends
        PartialBind.Base<EXTR, DPND, REMAP, FINAL>,
        PartialBind.Grouped<DPND>,
        PartialBind.Extractor<EXTR>,
        PartialBind.Remapper<EXTR, DPND, REMAP>,
        PartialBind.Finisher<REMAP, FINAL> {
}
