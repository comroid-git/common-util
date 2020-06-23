package org.comroid.test.trie;

import org.comroid.trie.TrieMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.nanoTime;

public class TrieMapTest {
    public static final int TEST_SIZE = 5000;
    private Set<UUID> ids;
    private TrieMap<String, UUID> trie;

    @Before
    public void setup() {
        trie = TrieMap.ofString();

        Map<String, List<UUID>> map = IntStream.range(0, TEST_SIZE)
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
        assertions();
    }

    // amount of calls made
    // multiplied by
    // 75 as the maximum average timeout
    @Test(timeout = (TEST_SIZE / 50) * 150)
    public void testPerformance() {
        System.out.printf("Starting TrieMap performance test at %d with %d ms timeout\n", nanoTime(), (TEST_SIZE / 50) * 75);

        IntStream.range(0, TEST_SIZE / 50)
                .sequential()
                .mapToLong(x -> nanoTime())
                .peek(x -> assertions())
                .map(x -> nanoTime() - x)
                .map(TimeUnit.NANOSECONDS::toMillis)
                .mapToObj(x -> {
                    if (x > 200)
                        return String.format("Assertions for %d IDs took a long time; finished in %d ms", TEST_SIZE, x);
                    return String.format("Assertions for %d IDs finished in %d ms", TEST_SIZE, x);
                })
                .forEachOrdered(System.out::println);
    }

    private void assertions() {
        Assert.assertEquals(TEST_SIZE, trie.size());

        final int equal = trie.biPipe()
                .filter((str, id) -> id.toString().equals(str))
                .span()
                .size();
        Assert.assertEquals(trie.size(), equal);

        ids.forEach(uuid -> {
            final String str = uuid.toString();

            Assert.assertTrue(trie.containsKey(str));

            final UUID value = trie.get(str);

            Assert.assertEquals(uuid, value);
        });
    }
}
