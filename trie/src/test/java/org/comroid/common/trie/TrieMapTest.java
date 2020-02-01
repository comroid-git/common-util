package org.comroid.common.trie;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrieMapTest {
    private TrieMap<String, Integer> trie;

    @Before
    public void setup() {
        trie = new TrieMap<>();

        trie.put("balloon", 99);
        trie.put("barrel", 50);
        trie.put("musik", 77);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testGet() {
        assertEquals(50, (int) trie.get("barrel"));
        assertEquals(99, (int) trie.get("balloon"));
        assertEquals(77, (int) trie.get("musik"));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(trie.isEmpty());
    }

    @Test
    public void testContainsKey() {
        assertTrue(trie.containsKey("barrel"));
        assertTrue(trie.containsKey("musik"));
        assertTrue(trie.containsKey("balloon"));

        assertFalse(trie.containsKey("büro"));
    }

    @Test
    public void testContainsValue() {
        assertTrue(trie.containsValue(77));
        assertTrue(trie.containsValue(50));
        assertTrue(trie.containsValue(99));

        assertFalse(trie.containsValue(20));
    }
}