package org.comroid.uniform.data;

import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;
import org.comroid.uniform.data.node.UniObjectNode;

public abstract class SerializationAdapter<BAS, OBJ extends BAS, ARR extends BAS> {
    public final DataStructureType.Obj<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType;
    public final DataStructureType.Arr<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType;

    public static SerializationAdapter<?, ?, ?> autodetect() {
        throw new UnsupportedOperationException();
    }

    protected SerializationAdapter(
            Class<OBJ> objClass,
            Class<ARR> arrClass
    ) {
        this(
                new DataStructureType.Obj<>(objClass),
                new DataStructureType.Arr<>(arrClass)
        );
    }

    protected SerializationAdapter(
            DataStructureType.Obj<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType,
            DataStructureType.Arr<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType
    ) {
        this.objectType = objectType;
        this.arrayType = arrayType;
    }

    @Override
    public String toString() {
        return String.format("%s{object=%s;array=%s}", getClass().getSimpleName(), objectType.tarClass.getName(), arrayType.tarClass.getName());
    }

    public abstract UniNode parse(String data);

    public abstract UniObjectNode createUniObjectNode(OBJ node);

    public abstract UniArrayNode createUniArrayNode(ARR node);

    public <TAR extends BAS> DataStructureType<SerializationAdapter<BAS, OBJ, ARR>, BAS, TAR> typeOf(TAR node) {
        if (objectType.typeClass().isInstance(node))
            return (DataStructureType<SerializationAdapter<BAS, OBJ, ARR>, BAS, TAR>) objectType;
        if (arrayType.typeClass().isInstance(node))
            return (DataStructureType<SerializationAdapter<BAS, OBJ, ARR>, BAS, TAR>) arrayType;

        throw new IllegalArgumentException("Unknown type: " + node.getClass().getName());
    }

    public final UniNode createUniNode(Object node) {
        if (node instanceof CharSequence)
            return parse(node.toString());

        if (objectType.typeClass().isInstance(node))
            return createUniObjectNode((OBJ) node);
        if (arrayType.typeClass().isInstance(node))
            return createUniArrayNode((ARR) node);

        throw new IllegalArgumentException(String.format("Unknown node type: %s", node.getClass().getName()));
    }

}
