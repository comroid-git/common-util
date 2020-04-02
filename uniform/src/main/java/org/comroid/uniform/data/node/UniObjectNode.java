package org.comroid.uniform.data.node;

import java.util.Map;

import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.data.SeriLib;

public interface UniObjectNode<BAS, OBJ extends BAS, MT>
        extends UniNode<BAS, OBJ>, Map<String, MT> {
    @Override
    default Primitive getType() {
        return Primitive.OBJECT;
    }

    default <CT, ARR extends BAS> UniArrayNode<BAS, ARR, CT> getUniArray(String key) {
        final SeriLib<BAS, OBJ, ARR> seriLib = (SeriLib<BAS, OBJ, ARR>) getSeriLib();
        final MT                     value   = get(key);

        final Class<ARR> arrClass = seriLib.arrayType.typeClass();
        if (arrClass.isInstance(value)) {
            return seriLib.createUniArrayNode((ARR) value);
        }

        throw new ClassCastException("Value is not an array");
    }
}
