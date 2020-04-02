package org.comroid.uniform.data.model;

import org.comroid.uniform.data.SeriLib;

public interface UniNodeExtensions<BAS, TAR extends BAS> {
    TAR getBaseNode();

    SeriLib<BAS, ? extends BAS, ? extends BAS> getSeriLib();
}
