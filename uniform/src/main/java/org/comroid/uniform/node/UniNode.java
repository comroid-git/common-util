package org.comroid.uniform.node;

import org.comroid.common.info.MessageSupplier;
import org.comroid.common.ref.Reference;
import org.comroid.common.ref.Specifiable;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class UniNode implements Specifiable<UniNode> {
    protected final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final Type type;

    public String getSerializedString() {
        return toString();
    }

    public abstract Object getBaseNode();

    public final SerializationAdapter<?, ?, ?> getSerializationAdapter() {
        return serializationAdapter;
    }

    public final boolean isObjectNode() {
        return getType() == Type.OBJECT;
    }

    public final Type getType() {
        return type;
    }

    public final boolean isArrayNode() {
        return getType() == Type.ARRAY;
    }

    public final boolean isValueNode() {
        return getType() == Type.VALUE;
    }

    public boolean isNull() {
        return this instanceof UniValueNode.Null;
    }

    protected UniNode(SerializationAdapter<?, ?, ?> serializationAdapter, Type type) {
        this.serializationAdapter = serializationAdapter;
        this.type = type;
    }

    public @NotNull Optional<UniNode> wrap(int index) {
        return has(index) ? Optional.of(get(index)) : Optional.empty();
    }

    public boolean has(int index) {
        return size() < index;
    }

    public abstract @NotNull UniNode get(int index);

    public abstract int size();

    public boolean isNull(String fieldName) {
        return wrap(fieldName).map(UniNode::isNull)
                .orElse(true);
    }

    public @NotNull Optional<UniNode> wrap(String fieldName) {
        return has(fieldName) ? Optional.of(get(fieldName)) : Optional.empty();
    }

    public abstract boolean has(String fieldName);

    public abstract @NotNull UniNode get(String fieldName);

    // todo: add helper methods
    public <T> @NotNull UniValueNode<T> add(ValueType<T> type, T value) throws UnsupportedOperationException {
        return put(size(), type, value);
    }

    public <T> @NotNull UniValueNode<T> put(int index, ValueType<T> type, T value) throws UnsupportedOperationException {
        return unsupported("PUT_INDEX", Type.ARRAY);
    }

    public <T> @NotNull UniValueNode<T> put(String key, ValueType<T> type, T value) throws UnsupportedOperationException {
        return unsupported("PUT_KEY", Type.OBJECT);
    }

    public @NotNull UniNode addNull() throws UnsupportedOperationException {
        return putNull(size());
    }

    public @NotNull UniNode putNull(int index) throws UnsupportedOperationException {
        return unsupported("PUT_NULL_INDEX", Type.ARRAY);
    }

    public @NotNull UniNode putNull(String key) throws UnsupportedOperationException {
        return unsupported("PUT_NULL_KEY", Type.OBJECT);
    }

    public @NotNull UniObjectNode addObject() throws UnsupportedOperationException {
        return putObject(size());
    }

    public @NotNull UniObjectNode putObject(int index) throws UnsupportedOperationException {
        return unsupported("PUT_OBJECT_INDEX", Type.ARRAY);
    }

    public @NotNull UniObjectNode putObject(String key) throws UnsupportedOperationException {
        return unsupported("PUT_OBJECT_KEY", Type.OBJECT);
    }

    public @NotNull UniArrayNode addArray() throws UnsupportedOperationException {
        return putArray(size());
    }

    public @NotNull UniArrayNode putArray(int index) throws UnsupportedOperationException {
        return unsupported("PUT_ARRAY_INDEX", Type.ARRAY);
    }

    public @NotNull UniArrayNode putArray(String key) throws UnsupportedOperationException {
        return unsupported("PUT_ARRAY_KEY", Type.ARRAY);
    }

    public Object asRaw(@Nullable Object fallback) {
        if (isNull() && fallback != null) {
            return fallback;
        }

        return unsupported("GET_RAW", Type.VALUE);
    }

    protected final <T> T unsupported(String actionName, Type expected) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(String.format("Cannot invoke %s on node type %s; " + "%s expected",
                actionName,
                getType(),
                expected
        ));
    }

    public <R> R as(ValueType<R> type) {
        return unsupported("GET_AS", Type.VALUE);
    }

    public String asString(@Nullable String fallback) {
        if (isNull() && fallback != null) {
            return fallback;
        }

        return unsupported("GET_AS_STRING", Type.VALUE);
    }

    public boolean asBoolean(boolean fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_BOOLEAN", Type.VALUE);
    }

    public int asInt(int fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_INT", Type.VALUE);
    }

    public long asLong(long fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_LONG", Type.VALUE);
    }

    public double asDouble(double fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_DOUBLE", Type.VALUE);
    }

    public float asFloat(float fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_FLOAT", Type.VALUE);
    }

    public short asShort(short fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_SHORT", Type.VALUE);
    }

    public char asChar(char fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_CHAR", Type.VALUE);
    }

    public List<Object> asList() {
        return unsupported("GET_AS_LIST", Type.ARRAY);
    }

    public List<? extends UniNode> asNodeList() {
        return unsupported("GET_AS_NODELIST", Type.ARRAY);
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

    protected <T> UniValueNode.Adapter<T> makeValueAdapter(Supplier<String> stringSupplier) {
        return new UniValueNode.Adapter.ViaString<>(stringSupplier::get);
    }

    @NotNull
    protected <T> UniValueNode<T> generateValueNode(String ofString) {
        final UniValueNode.Adapter.ViaString<T> valueAdapter
                = new UniValueNode.Adapter.ViaString<>(Reference.constant(ofString));
        return new UniValueNode<>(serializationAdapter, valueAdapter);
    }

    public enum Type {
        OBJECT,
        ARRAY,
        VALUE
    }

    public interface Adapter<B> {
        B getBaseNode();
    }
}
