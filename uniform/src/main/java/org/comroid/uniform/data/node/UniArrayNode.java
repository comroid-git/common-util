package org.comroid.uniform.data.node;

import java.util.List;

import org.comroid.uniform.data.DataStructureType.Primitive;

public interface UniArrayNode<BAS, ARR extends BAS, CT> extends UniNode<BAS>, List<CT> {
    @Override
    default Primitive getType() {
        return Primitive.ARRAY;
    }
}
