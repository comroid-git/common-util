package org.comroid.test.common.ref;

import org.comroid.mutatio.pipe.Pump;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PumpTests {
    private List<String> controlGroup;

    @Before
    public void setup() {
        controlGroup = Collections.unmodifiableList(IntStream.range(0, 50)
                .mapToObj(txt -> UUID.randomUUID())
                .map(UUID::toString)
                .collect(Collectors.toList()));
    }

    @Test
    public void testBasicOperations() throws InterruptedException {
        final Pump<String, String> remapOp = Pump.<String>create().map(String::toLowerCase);
        controlGroup.forEach(remapOp);
        for (int i = 0; i < controlGroup.size(); i++)
            Assert.assertEquals("index " + i, controlGroup.get(i).toLowerCase(), remapOp.get(i));

        final Pump<String, String> filterOp = Pump.<String>create()
                .filter(str -> str.chars()
                        .map(Character::toLowerCase)
                        .allMatch(c -> c != 'a'));
        controlGroup.forEach(filterOp);
        for (int i = 0; i < filterOp.size(); i++)
            filterOp.getReference(i)
                    .wrap()
                    .map(String::toLowerCase)
                    .ifPresent(str -> Assert.assertFalse(str.contains("a")));

        final Pump<String, String> filterMapOp = Pump.<String>create()
                .map(String::toLowerCase)
                .filter(str -> str.chars()
                        .allMatch(c -> c != 'a'));
        controlGroup.forEach(filterMapOp);
        for (int i = 0; i < filterOp.size(); i++)
            filterOp.getReference(i)
                    .wrap()
                    .ifPresent(str -> Assert.assertFalse(str.contains("a")));
    }
}
