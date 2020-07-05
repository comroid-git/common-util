package org.comroid.uniform.node;

import org.comroid.api.Specifiable;
import org.comroid.common.info.MessageSupplier;
import org.comroid.mutatio.proc.Processor;
import org.comroid.mutatio.ref.Reference;
import org.comroid.trie.TrieMap;
import org.comroid.uniform.DataStructureType;
import org.comroid.uniform.HeldType;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.ValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class UniNode implements Specifiable<UniNode> {
    protected final SerializationAdapter<?, ?, ?> serializationAdapter;
    private final Type type;
    private final Map<String, Reference.Settable<String>> baseAccessors = TrieMap.ofString();
    private final Map<String, Processor<UniNode>> wrappedAccessors = TrieMap.ofString();

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

    public abstract int size();

    public boolean isNull(String fieldName) {
        return wrap(fieldName).map(UniNode::isNull)
                .orElse(true);
    }

    public abstract @NotNull UniNode get(int index);

    public abstract @NotNull UniNode get(String fieldName);

    public @NotNull Optional<UniNode> wrap(int index) {
        return has(index) ? Optional.of(get(index)) : Optional.empty();
    }

    public @NotNull Optional<UniNode> wrap(String fieldName) {
        return has(fieldName) ? Optional.of(get(fieldName)) : Optional.empty();
    }

    public @NotNull Processor<UniNode> process(int index) {
        return Processor.ofConstant(get(index));
    }

    public @NotNull Processor<UniNode> process(String fieldName) {
        return Processor.ofConstant(get(fieldName));
    }

    public boolean has(int index) {
        return size() < index;
    }

    public abstract boolean has(String fieldName);

    // todo: add helper methods
    public @NotNull <T> UniNode add(HeldType<T> type, T value) throws UnsupportedOperationException {
        return put(size(), type, value);
    }

    public @NotNull <T> UniNode put(int index, HeldType<T> type, T value) throws UnsupportedOperationException {
        return unsupported("PUT_INDEX", Type.ARRAY);
    }

    protected String unwrapDST(Object o) {
        if (o instanceof UniNode)
            return o.toString();
        return String.valueOf(o);
    }

    public @NotNull <T> UniNode put(String key, HeldType<T> type, T value) throws UnsupportedOperationException {
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

    @Contract(value = "_ -> this", mutates = "this")
    public abstract UniNode copyFrom(@NotNull UniNode it);

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

    public String asString() {
        return asString(null);
    }

    public String asString(@Nullable String fallback) {
        if (isNull() && fallback != null) {
            return fallback;
        }

        return unsupported("GET_AS_STRING", Type.VALUE);
    }

    public boolean asBoolean() {
        return asBoolean(false);
    }

    public boolean asBoolean(boolean fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_BOOLEAN", Type.VALUE);
    }

    public int asInt() {
        return asInt(0);
    }

    public int asInt(int fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_INT", Type.VALUE);
    }

    public long asLong() {
        return asLong(0);
    }

    public long asLong(long fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_LONG", Type.VALUE);
    }

    public double asDouble() {
        return asDouble(0);
    }

    public double asDouble(double fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_DOUBLE", Type.VALUE);
    }

    public float asFloat() {
        return asFloat(0);
    }

    public float asFloat(float fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_FLOAT", Type.VALUE);
    }

    public short asShort() {
        return asShort((short) 0);
    }

    public short asShort(short fallback) {
        if (isNull()) {
            return fallback;
        }

        return unsupported("GET_AS_SHORT", Type.VALUE);
    }

    public char asChar() {
        return asChar((char) 0);
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

    @Override
    public final String toString() {
        return getBaseNode().toString();
    }

    protected Processor<UniNode> computeNode(
            String fieldName,
            Supplier<Reference.Settable<String>> stringReferenceSupplier
    ) {
        final Reference.Settable<String> base
                = baseAccessors.computeIfAbsent(fieldName, key -> stringReferenceSupplier.get());
        final Processor<UniNode> wrapped
                = wrappedAccessors.computeIfAbsent(fieldName, key -> base.process()
                .map(str -> {
                    final DataStructureType<? extends SerializationAdapter<?, ?, ?>, ?, ?> dst = serializationAdapter.typeOfData(str);

                    if (dst != null) switch (dst.typ) {
                        case OBJECT:
                            return serializationAdapter.parse(str).asObjectNode();
                        case ARRAY:
                            return serializationAdapter.parse(str).asArrayNode();
                    }

                    final UniValueNode.Adapter.ViaString adapter
                            = new UniValueNode.Adapter.ViaString(base);
                    final UniValueNode<String> valueNode
                            = new UniValueNode<>(serializationAdapter, adapter);
                    return valueNode;
                }));

        return wrapped;
    }

    protected void set(Object value) {
        unsupported("SET", Type.VALUE);
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
