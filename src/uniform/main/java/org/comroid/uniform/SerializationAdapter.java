package org.comroid.uniform;

import org.comroid.api.Polyfill;
import org.comroid.common.io.FileHandle;
import org.comroid.util.ReflectionHelper;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.UniValueNode;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class SerializationAdapter<BAS, OBJ extends BAS, ARR extends BAS> {
    public final DataStructureType.Arr<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType;
    public final DataStructureType.Obj<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType;
    private final String mimeType;

    public final String getMimeType() {
        return mimeType;
    }

    protected SerializationAdapter(
            String mimeType, Class<OBJ> objClass, Class<ARR> arrClass
    ) {
        this(mimeType, new DataStructureType.Obj<>(objClass), new DataStructureType.Arr<>(arrClass));

        Objects.requireNonNull(objClass, "Object class cannot be null");
    }

    protected SerializationAdapter(
            String mimeType,
            DataStructureType.Obj<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType,
            DataStructureType.Arr<SerializationAdapter<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType
    ) {
        this.mimeType = mimeType;
        this.objectType = objectType;
        this.arrayType = arrayType;
    }

    public static SerializationAdapter<?, ?, ?> autodetect() {
        throw new UnsupportedOperationException();
    }

    public final UniNode readFile(FileHandle file) {
        return createUniNode(file.getContent());
    }

    @Override
    public String toString() {
        return String.format(
                "%s{object=%s;array=%s}",
                getClass().getSimpleName(),
                objectType.tarClass.getName(),
                arrayType.tarClass.getName()
        );
    }

    public <TAR extends BAS> DataStructureType<SerializationAdapter<BAS, OBJ, ARR>, BAS, TAR> typeOf(
            TAR node
    ) {
        if (objectType.typeClass()
                .isInstance(node)) {
            return (DataStructureType<SerializationAdapter<BAS, OBJ, ARR>, BAS, TAR>) objectType;
        }
        if (arrayType.typeClass()
                .isInstance(node)) {
            return (DataStructureType<SerializationAdapter<BAS, OBJ, ARR>, BAS, TAR>) arrayType;
        }

        throw new IllegalArgumentException("Unknown type: " + node.getClass()
                .getName());
    }

    public final UniNode createUniNode(Object node) {
        if (node == null)
            UniValueNode.nullNode();

        if (node instanceof CharSequence) {
            return parse(node == null ? null : node.toString());
        }

        if (objectType.typeClass()
                .isInstance(node)) {
            return createUniObjectNode((OBJ) node);
        }
        if (arrayType.typeClass()
                .isInstance(node)) {
            return createUniArrayNode((ARR) node);
        }

        throw new IllegalArgumentException(String.format(
                "Unknown node type: %s",
                node.getClass()
                        .getName()
        ));
    }

    public abstract UniNode parse(@Nullable String data);

    public UniObjectNode createUniObjectNode() {
        return createUniObjectNode(objectType.get());
    }

    public abstract UniObjectNode createUniObjectNode(OBJ node);

    public UniArrayNode createUniArrayNode() {
        return createUniArrayNode(arrayType.get());
    }

    public abstract UniArrayNode createUniArrayNode(ARR node);

}
