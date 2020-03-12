package org.comroid.uniform.data;

public abstract class SeriLib<BAS, OBJ extends BAS, ARR extends BAS> {
    protected final DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ> objectType;
    protected final DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, ARR> arrayType;

    protected SeriLib(
            DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ> objectType,
            DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, ARR> arrayType
    ) {
        this.objectType = objectType;
        this.arrayType = arrayType;
    }

    protected SeriLib(Class<OBJ> objClass, Class<ARR> arrClass) {
        this(new DataStructureType.Obj<>(objClass), new DataStructureType.Arr<>(arrClass));
    }

    public DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, ? extends BAS> typeOf(BAS node) {
        if (objectType.typeClass().isInstance(node))
            return objectType;
        if (arrayType.typeClass().isInstance(node))
            return arrayType;

        throw new IllegalArgumentException("Unknown type: " + node.getClass().getName());
    }

    public final DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ> objectType() {
        return objectType;
    }

    public final DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, ARR> arrayType() {
        return arrayType;
    }
}
