package org.comroid.trie;

import org.comroid.api.Polyfill;
import org.comroid.mutatio.ref.KeyedReference;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TrieStage<K, V> {
    @SuppressWarnings("unchecked")
    protected final TrieStage<K, V>[] storage = new TrieStage[Character.MAX_CODE_POINT];
    protected final K key;
    protected final String keyRep;
    protected final KeyedReference<K, V> reference;
    protected final TrieMap<K, V> base;
    protected final @Nullable
    TrieStage<K, V> parent;

    protected TrieStage(K myKey, String keyRep) {
        if (!(this instanceof TrieMap))
            throw new UnsupportedOperationException("Illegal constructor call");

        this.base = Polyfill.uncheckedCast(this);
        this.parent = null;
        this.key = myKey;
        this.keyRep = keyRep;
        this.reference = KeyedReference.create(myKey);
    }

    protected TrieStage(TrieMap<K, V> base, TrieStage<K, V> parent, K myKey, String keyRep) {
        this.base = base;
        this.parent = parent;
        this.key = myKey;
        this.keyRep = keyRep;
        this.reference = KeyedReference.create(myKey);
    }

    protected @Nullable
    KeyedReference<K, V> getReference(
            String stringKey,
            char[] chars,
            int index,
            boolean createIfAbsent
    ) {
        final char c = chars[index];
        TrieStage<K, V> next = storage[c];

        if (next == null) {
            if (createIfAbsent) {
                final String nextKey = keyRep + c;
                final K nextKeyConverted = base.getKeyConverter().backward(nextKey);
                storage[c] = next = new TrieStage<>(base, this, nextKeyConverted, nextKey);
            } else return null;
        }

        if (index + 1 >= chars.length && next.keyRep.equals(stringKey))
            return next.reference;

        return next.getReference(stringKey, chars, index + 1, createIfAbsent);
    }
}
