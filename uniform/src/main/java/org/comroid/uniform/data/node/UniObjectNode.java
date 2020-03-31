package org.comroid.uniform.data.node;

import java.util.Map;

import org.comroid.uniform.data.DataStructureType.Primitive;

public interface UniObjectNode<BAS, OBJ extends BAS, MT> extends UniNode<BAS>, Map<String, MT> {
    @Override
    default Primitive getType() {
        return Primitive.OBJECT;
    }
}
