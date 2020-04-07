package org.comroid.uniform.node;

import java.util.UUID;

import org.comroid.common.trie.TrieMap;

public abstract class UniNode {
    static <T> String selfCacheAccessKey(UUID identifier, String fieldName, Class<T> asType) {
        return String.format("%s-%s-%s", identifier, fieldName, asType.getName());
    }

    protected final static class API {
        private final TrieMap<String, UniObjectNode>   objectNodeCache = TrieMap.ofString();
        private final TrieMap<String, UniArrayNode>    arrayNodeCache  = TrieMap.ofString();
        private final TrieMap<String, UniValueNode<?>> valueNodeCache  = TrieMap.ofString();

        public <T> UniValueNode<T> objectAccessingValueNode(
                UUID identifier, String fieldName, Class<T> asType
        ) {
        }
    }

    public final Type getType() {
        return type;
    }

    protected final String identifier;
    protected final API    API;
    private final   Type   type;

    protected UniNode(API API, Type type) {
        this.API        = API;
        this.type       = type;
        this.identifier = UUID.randomUUID()
                              .toString();
        selfCache.put(identifier, this);
    }

    public enum Type {
        OBJECT,
        ARRAY,
        VALUE
    }
}
