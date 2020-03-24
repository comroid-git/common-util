package org.comroid.test.common.iter;

import java.util.Arrays;
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
    private static final Random rng   = new Random();
    private static       int    bound = rng.nextInt(100) + 50;

    private Span<String>                span;
    private List<Pair<String, Integer>> generated;

    @Test
    public void testDefaults() {
        this.span = Span.<String>make().span();

        this.generated = IntStream.range(0, bound)
                                  .mapToObj(c -> UUID.randomUUID()
                                                     .toString())
                                  .map(str -> new Pair<>(str, rng.nextInt() % bound))
                                  .sorted(Comparator.comparingInt(Pair::getSecond))
                                  .collect(Collectors.toList());
        System.out.printf("generated        = {%d}%s%n", generated.size(), generated);

        int added = (int) generated.stream()
                                   .map(Pair::getFirst)
                                   .filter(span::add)
                                   .count();
        System.out.printf("added to span    = %d successful%n", added);

        System.out.println("span             = " + span);

        int cContains = (int) generated.stream()
                                       .map(Pair::getFirst)
                                       .filter(span::contains)
                                       .count();
        System.out.println("span contains    = " + cContains);
        System.out.println("span             = " + span);

        span.cleanup();

        System.out.println("span cleanup     = " + span);

        assertTrue(bound <= span.size());
        assertTrue(span.contains(randomGenerated()));

        final String removeThis = randomGenerated();
        final int    count      = span.count(removeThis);
        System.out.println(
                "removing value   = " + removeThis + "; found " + count + " occurrence" + (count == 1
                        ? ""
                        : "s"));
        assertTrue(span.remove(removeThis));
        System.out.println("span after rem   = " + span);
        bound -= 1;

        System.out.println("span before      = " + span);

        final int size_beforeBulk = span.size();
        final int remove          = (size_beforeBulk / 4) + (size_beforeBulk / 2);
        System.out.printf("removing values  = [%d]{", remove);
        final long successful = IntStream.range(0, remove)
                                         .sequential()
                                         .mapToObj(nil -> randomGenerated())
                                         .peek(it -> System.out.printf(" %s,", it))
                                         .map(it -> span.remove(it))
                                         .filter(Boolean::booleanValue)
                                         .count();

        System.out.printf(
                "};\n" + "                   ... %d were successful.\n" + "remove           = %d%n",
                successful, remove
        );

        final Object[] iterable = span.toArray();
        System.out.println("span after       = " + span);
        System.out.printf(
                "span iterable    = {%d}%s%n", iterable.length, Arrays.toString(iterable));

        assertEquals(successful, remove);
        //assertEquals(size_beforeBulk - successful, iterable.length);

        span.cleanup();
        System.out.printf("span cleanup     = %s%n", span);

        assertEquals((bound -= remove), span.size());
    }

    private String randomGenerated() {
        return generated.remove(Math.abs((rng.nextInt() + 1) % generated.size()))
                        .getFirst();
    }
}
