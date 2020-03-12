package org.comroid.uniform.data;

public class DataStructureType<SERI extends SeriLib<BAS, ?, ?>, BAS, TAR extends BAS> {
    public final Primitive typ;
    private final Class<TAR> tarClass;

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

        throw new ClassCastException(String.format("Cannot cast %s to primitive type %s %s", node, typ.name(), tarClass.getName()));
    }

    @Override
    public int hashCode() {
        return (31 * tarClass.hashCode()) + typ.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataStructureType<?, ?, ?> that = (DataStructureType<?, ?, ?>) o;

        if (!tarClass.equals(that.tarClass))
            return false;
        return typ == that.typ;
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
            super(arrClass, Primitive.ARRAY);
        }
    }

    public enum Primitive {
        OBJECT,
        ARRAY
    }
}
