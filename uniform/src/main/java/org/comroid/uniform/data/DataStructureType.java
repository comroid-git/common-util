package org.comroid.uniform.data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class DataStructureType<SERI extends SeriLib<BAS, ?, ?>, BAS, TAR extends BAS> {
    public final Primitive typ;
    protected final Class<TAR> tarClass;

    protected DataStructureType(Class<TAR> tarClass, Primitive typ) {
        this.tarClass = tarClass;
        this.typ = typ;
    }

    public Class<TAR> typeClass() {
        return tarClass;
    }

    public TAR cast(Object node) throws ClassCastException {
        if (tarClass.isInstance(node))
            return tarClass.cast(node);

        throw new ClassCastException(String.format("Cannot cast %s to targeted %s type %s", node.getClass().getName(), typ.name(), tarClass.getName()));
    }

    @Override
    public int hashCode() {
        return (31 * tarClass.hashCode()) + typ.hashCode();
    }

    @Override
    public String toString() {
        return String.format("DataStructureType{typ=%s, tarClass=%s}", typ, tarClass);
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

    public static class Obj<SERI extends SeriLib<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS>
            extends DataStructureType<SERI, BAS, OBJ> {
        public Obj(Class<OBJ> objClass) {
            super(objClass, Primitive.OBJECT);
        }
    }

    public static class Arr<SERI extends SeriLib<BAS, OBJ, ARR>, BAS, OBJ extends BAS, ARR extends BAS>
            extends DataStructureType<SERI, BAS, ARR> {
        private final ToIntFunction<ARR> sizeEvaluation;
        private final Function<BAS, List<BAS>> splitter;

        public Arr(Class<ARR> arrClass, ToIntFunction<ARR> sizeEvaluation, Function<BAS, List<BAS>> splitter) {
            super(arrClass, Primitive.ARRAY);
            this.sizeEvaluation = sizeEvaluation;
            this.splitter = splitter;
        }

        public final int sizeOf(ARR array) {
            return sizeEvaluation.applyAsInt(array);
        }

        public final List<BAS> split(BAS node) {
            final ARR cast;

            try {
                cast = cast(node);
            } catch (ClassCastException ignored) {
                // "node" is object
                return Collections.singletonList(node);
            }

            return splitter.apply(cast);
        }
    }

    public enum Primitive {
        OBJECT,
        ARRAY
    }
}
