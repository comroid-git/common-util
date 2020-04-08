package org.comroid.uniform.node;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.comroid.common.info.MessageSupplier;
import org.comroid.common.ref.Specifiable;
import org.comroid.uniform.data.SeriLib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class UniNode implements Specifiable<UniNode> {
    protected final SeriLib<?, ?, ?> seriLib;
    private final Type type;

    protected UniNode(SeriLib<?, ?, ?> seriLib, Type type) {
        this.seriLib = seriLib;
        this.type = type;
    }

    public abstract @NotNull UniNode get(String fieldName);

    public abstract @NotNull UniNode get(int index);

    public @NotNull Optional<UniNode> wrap(String fieldName) {
        return has(fieldName) ? Optional.of(get(fieldName)) : Optional.empty();
    }

    public @NotNull Optional<UniNode> wrap(int index) {
        return has(index) ? Optional.of(get(index)) : Optional.empty();
    }

    public abstract int size();

    public abstract boolean has(String fieldName);

    public boolean has(int index) {
        return size() < index;
    }

    public boolean isNull(String fieldName) {
        return wrap(fieldName).map(UniNode::isNull).orElse(true);
    }

    public boolean isNull() {
        return this instanceof UniValueNode.Null;
    }

    public Object asRaw(@Nullable Object fallback) {
        if (isNull() && fallback != null)
            return fallback;

        return unsupported("GET_RAW", Type.VALUE);
    }

    public String asString(@Nullable String fallback) {
        if (isNull() && fallback != null)
            return fallback;

        return unsupported("GET_AS_STRING", Type.VALUE);
    }

    public boolean asBoolean(boolean fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_BOOLEAN", Type.VALUE);
    }

    public int asInt(int fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_INT", Type.VALUE);
    }

    public long asLong(long fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_LONG", Type.VALUE);
    }

    public double asDouble(double fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_DOUBLE", Type.VALUE);
    }

    public float asFloat(float fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_FLOAT", Type.VALUE);
    }

    public short asShort(short fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_SHORT", Type.VALUE);
    }

    public char asChar(char fallback) {
        if (isNull())
            return fallback;

        return unsupported("GET_AS_CHAR", Type.VALUE);
    }

    public String getSerializedString() {
        return toString();
    }

    public List<Object> asList() {
        return unsupported("GET_AS_LIST", Type.ARRAY);
    }

    public List<UniNode> asNodeList() {
        return unsupported("GET_AS_NODELIST", Type.ARRAY);
    }

    protected <T> UniValueNode.Adapter<T> makeValueAdapter(Supplier<String> stringSupplier) {
        return new UniValueNode.Adapter.ViaString<>(stringSupplier::get);
    }

    protected final <T> T unsupported(String actionName, Type expected) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(String.format("Cannot invoke %s on node type %s; %s expected", actionName, getType(), expected));
    }

    public final SeriLib<?, ?, ?> getSeriLib() {
        return seriLib;
    }

    public final Type getType() {
        return type;
    }

    public final boolean isObjectNode() {
        return getType() == Type.OBJECT;
    }

    public final boolean isArrayNode() {
        return getType() == Type.ARRAY;
    }

    public final boolean isValueNode() {
        return getType() == Type.VALUE;
    }

    public final UniObjectNode asObjectNode() {
        return as(UniObjectNode.class, MessageSupplier.format("Node is of %s type; expected %s", getType(), Type.OBJECT));
    }

    public final UniArrayNode asArrayNode() {
        return as(UniArrayNode.class, MessageSupplier.format("Node is of %s type; expected %s", getType(), Type.ARRAY));
    }

    public final <T> UniValueNode<T> asValueNode() {
        return as(UniValueNode.class, MessageSupplier.format("Node is of %s type; expected %s", getType(), Type.VALUE));
    }

    public abstract Object getBaseNode();

    public interface Adapter {
        Object getBaseNode();
    }

    public enum Type {
        OBJECT,
        ARRAY,
        VALUE
    }
}
