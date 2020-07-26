package org.comroid.test.trie;

import org.comroid.trie.TrieMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        assertions(trie);
    }

    // amount of calls made
    // multiplied by
    // 75 as the maximum average timeout
    @Test
    public void testPerformance() {
        runPerformanceTest(trie);
        runPerformanceTest(new ConcurrentHashMap<>(trie));
    }

    private void runPerformanceTest(Map<String, UUID> map) {
        System.out.printf("Starting %s performance test at %d with %d ms timeout\n",
                map.getClass().getSimpleName(),
                nanoTime(),
                (TEST_SIZE / 50) * 75);

        IntStream.range(0, TEST_SIZE / 50)
                .sequential()
                .mapToLong(x -> nanoTime())
                .peek(x -> assertions(map))
                .map(x -> nanoTime() - x)
                .map(TimeUnit.NANOSECONDS::toMillis)
                .mapToObj(x -> {
                    if (x > 200)
                        return String.format("Assertions for %d IDs took a long time; finished in %d ms", TEST_SIZE, x);
                    return String.format("Assertions for %d IDs finished in %d ms", TEST_SIZE, x);
                })
                .forEachOrdered(System.out::println);
    }

    private void assertions(Map<String, UUID> test) {
        Assert.assertEquals(TEST_SIZE, test.size());

        final long equal = test.entrySet()
                .stream()
                .filter(entry -> entry.getValue().toString().equals(entry.getKey()))
                .count();
        Assert.assertEquals(test.size(), equal);

        ids.forEach(uuid -> {
            final String str = uuid.toString();

            Assert.assertTrue(test.containsKey(str));

            final UUID value = test.get(str);

            Assert.assertEquals(uuid, value);
        });
    }
}
