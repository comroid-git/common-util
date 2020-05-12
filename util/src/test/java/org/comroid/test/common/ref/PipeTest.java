package org.comroid.test.common.ref;

import org.comroid.common.ref.ReferenceIndex;
import org.comroid.common.ref.pipe.Pipe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PipeTest {
    private List<String> controlGroup;

    @Before
    public void setup() {
        controlGroup = Collections.unmodifiableList(IntStream.range(0, 50)
                .mapToObj(txt -> UUID.randomUUID())
                .map(UUID::toString)
                .collect(Collectors.toList()));
    }

    @Test
    public void testBasicOperations() {
        final ReferenceIndex<String> strings = ReferenceIndex.of(controlGroup);

        final Pipe<String, String> lowerCases = strings.pipe().map(String::toLowerCase);
        for (int i = 0; i < controlGroup.size(); i++)
            Assert.assertEquals("index " + i, controlGroup.get(0).toLowerCase(), lowerCases.get(i));
    }
}
