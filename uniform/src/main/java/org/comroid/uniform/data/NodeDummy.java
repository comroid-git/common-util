package org.comroid.uniform.data;

import org.comroid.common.func.bi.Junction;

import org.jetbrains.annotations.Nullable;

public abstract class NodeDummy<SERI extends SeriLib<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS, TAR extends BAS> {
    public final DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type;
    private final SERI seriLib;
    private final TAR node;

    NodeDummy(SERI seriLib, TAR node, DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> type) {
        this.seriLib = seriLib;
        this.node = node;
        this.type = type;
    }

    public abstract boolean containsKey(String name);

    public abstract <T> @Nullable T extract(Junction<TAR, T> converter) throws IllegalArgumentException;

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
