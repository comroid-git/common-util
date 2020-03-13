package org.comroid.uniform.data;

import java.util.Optional;
import java.util.function.Function;

import org.comroid.common.annotation.OptionalVararg;

public abstract class NodeDummy<SERI extends SeriLib<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS, TAR extends BAS> {
    public final DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type;
    private final SERI seriLib;
    private final TAR node;

    protected NodeDummy(SERI seriLib, TAR node, DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type) {
        this.seriLib = seriLib;
        this.node = node;
        this.type = type;
    }

    public abstract boolean containsKey(String name);

    private <T extends BAS> T tryReturn(DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, T> type) throws StructureTypeMismatchException {
        if (!type.equals(this.type))
            throw new StructureTypeMismatchException(type, this.type);

        return type.cast(node);
    }

    public final OBJ obj() throws StructureTypeMismatchException {
        return tryReturn(seriLib.objectType);
    }

    public final ARR arr() throws StructureTypeMismatchException {
        return tryReturn(seriLib.arrayType);
    }

    protected final <R> R process(Function<OBJ, R> objMapper, Function<ARR, R> arrMapper) {
        if (type.typ == DataStructureType.Primitive.OBJECT)
            return objMapper.apply(obj());
        if (type.typ == DataStructureType.Primitive.ARRAY)
            return arrMapper.apply(arr());

        throw new StructureTypeMismatchException(type);
    }
}
