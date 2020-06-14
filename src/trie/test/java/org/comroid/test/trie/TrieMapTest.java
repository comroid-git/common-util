package org.comroid.test.trie;

import org.comroid.trie.TrieMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrieMapTest {
    private Set<UUID> ids;
    private TrieMap<String, UUID> trie;

    @Before
    public void setup() {
        trie = TrieMap.ofString();

        Map<String, List<UUID>> map = IntStream.range(0, 50)
                .mapToObj(x -> UUID.randomUUID())
                .collect(Collectors.groupingBy(UUID::toString));

        ids = map.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        map.forEach((key, id) -> trie.put(key, id.get(0)));
    }

    @Test
    public void testBasic() {
        Assert.assertEquals(50, trie.size());

        ids.forEach(uuid -> {
            final String str = uuid.toString();

            Assert.assertTrue(trie.containsKey(str));

            final UUID value = trie.get(str);

            Assert.assertEquals(uuid, value);
        });
    }
}
