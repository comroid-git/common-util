package org.comroid.uniform.data.node;

import org.comroid.uniform.data.DataStructureType;
import org.comroid.uniform.data.model.UniNodeExtensions;

public interface UniNode<BAS, TAR extends BAS> extends UniNodeExtensions<BAS, TAR> {
    DataStructureType.Primitive getType();
}
