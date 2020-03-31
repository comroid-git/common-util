package org.comroid.uniform.data.node;

import org.comroid.uniform.data.DataStructureType;
import org.comroid.uniform.data.model.BaseNodeMember;

public interface UniNode<BAS> extends BaseNodeMember<BAS> {
    DataStructureType.Primitive getType();
}
