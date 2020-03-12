package org.comroid.uniform.data;

public final class NodeDummy<SERI extends SeriLib<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS, TAR extends BAS> {
    private final SERI seriLib;
    private final TAR node;
    public final DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type;

    NodeDummy(SERI seriLib, TAR node, DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type) {
        this.seriLib = seriLib;
        this.node = node;
        this.type = type;
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
