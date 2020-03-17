package org.comroid.uniform.data;

import java.util.function.Function;

public abstract class NodeDummy<SERI extends SeriLib<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS, TAR extends BAS> {
    public final DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type;
    private final SERI seriLib;
    private final TAR node;

    protected NodeDummy(SERI seriLib, TAR node) {
        this.seriLib = seriLib;
        this.node = node;
        this.type = seriLib.typeOf(node);
    }

    public abstract boolean containsKey(String name);

    public abstract <T> T getValueAs(String fieldName, Class<T> targetType);

    protected final <R> R process(Function<OBJ, R> objMapper, Function<ARR, R> arrMapper) {
        if (type.typ == DataStructureType.Primitive.OBJECT)
            return objMapper.apply(obj());
        if (type.typ == DataStructureType.Primitive.ARRAY)
            return arrMapper.apply(arr());

        throw new StructureTypeMismatchException(type);
    }

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
}
