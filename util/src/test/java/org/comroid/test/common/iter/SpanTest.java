package org.comroid.test.common.iter;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.comroid.common.iter.Span;
import org.comroid.common.ref.Pair;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpanTest {
    private static final Random rng = new Random();
    private static int bound = rng.nextInt(100);

    private Span<String> span;
    private List<Pair<String, Integer>> generated;

    @Test
    public void testSpan() {
        this.span = new Span<>(1, Span.NullPolicy.SKIP, true);

        this.generated = IntStream.range(0, bound)
                .mapToObj(c -> UUID.randomUUID().toString())
                .map(str -> new Pair<>(str, rng.nextInt() % bound))
                .sorted(Comparator.comparingInt(Pair::getSecond))
                .collect(Collectors.toList());
        generated.stream()
                .map(Pair::getFirst)
                .forEach(span::add);

        assertEquals(bound, span.size());
        assertTrue(span.contains(randomGenerated()));
        assertTrue(span.remove(randomGenerated()));
        bound -= 1;

        final List<String> remove = IntStream.range(0, 10)
                .sequential()
                .mapToObj(nil -> randomGenerated())
                .collect(Collectors.toList());
        assertTrue(span.removeAll(remove));
        assertEquals((bound -= 10), span.size());
    }

    private String randomGenerated() {
        return generated.remove(Math.abs(rng.nextInt() % generated.size())).getFirst();
    }
}
