package org.comroid.uniform.data;

public class DataStructureType<SERI extends SerializationAdapter<BAS, ?, ?>, BAS, TAR extends BAS> {
    public final Primitive typ;
    protected final Class<TAR> tarClass;

    protected DataStructureType(Class<TAR> tarClass, Primitive typ) {
        this.tarClass = tarClass;
        this.typ = typ;
    }

    @Override
    public int hashCode() {
        return (31 * tarClass.hashCode()) + typ.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataStructureType<?, ?, ?> that = (DataStructureType<?, ?, ?>) o;

        if (!tarClass.equals(that.tarClass)) return false;
        return typ == that.typ;
    }

    @Override
    public String toString() {
        return String.format("DataStructureType{typ=%s, tarClass=%s}", typ, tarClass);
    }

    public Class<TAR> typeClass() {
        return tarClass;
    }

    public TAR cast(Object node) throws ClassCastException {
        if (tarClass.isInstance(node)) return tarClass.cast(node);

        throw new ClassCastException(String.format("Cannot cast %s to targeted %s type %s",
                node.getClass()
                        .getName(), typ.name(), tarClass.getName()
        ));
    }

    public static class Obj<SERI extends SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS>
            extends DataStructureType<SERI, BAS, OBJ> {
        public Obj(Class<OBJ> objClass) {
            super(objClass, Primitive.OBJECT);
        }
    }

    public static class Arr<SERI extends SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS>
            extends DataStructureType<SERI, BAS, ARR> {

        public Arr(
                Class<ARR> arrClass
        ) {
            super(arrClass, Primitive.ARRAY);
        }
    }

    public enum Primitive {
        OBJECT,
        ARRAY
    }
}
