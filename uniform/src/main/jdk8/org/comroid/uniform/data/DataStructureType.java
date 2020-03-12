package org.comroid.uniform.data;

public class DataStructureType<SERI extends SeriLib<BAS, ?, ?>, BAS, TAR extends BAS> {
    private final Class<TAR> tarClass;
    private final Primitive typ;

    protected DataStructureType(Class<TAR> tarClass, Primitive typ) {
        this.tarClass = tarClass;
        this.typ = typ;
    }

    public Class<TAR> typeClass() {
        return tarClass;
    }

    public TAR cast(BAS node) throws ClassCastException {
        if (tarClass.isInstance(node))
            return tarClass.cast(node);

        throw new ClassCastException(String.format("Cannot cast %s to type %s", node, tarClass.getName()));
    }

    public final Primitive typ() {
        return typ;
    }

    public static class Obj<SERI extends SeriLib<BAS, TAR, ?>, BAS, TAR extends BAS>
            extends DataStructureType<SERI, BAS, TAR> {
        public Obj(Class<TAR> objClass) {
            super(objClass, Primitive.OBJECT);
        }
    }

    public static class Arr<SERI extends SeriLib<BAS, ?, TAR>, BAS, TAR extends BAS>
            extends DataStructureType<SERI, BAS, TAR> {
        public Arr(Class<TAR> arrClass) {
            super(arrClass, Primitive.OBJECT);
        }
    }

    public enum Primitive {
        OBJECT,
        ARRAY
    }
}
