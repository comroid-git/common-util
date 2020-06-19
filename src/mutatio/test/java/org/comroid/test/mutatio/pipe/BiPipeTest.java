package org.comroid.test.mutatio.pipe;

import org.comroid.mutatio.span.Span;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BiPipeTest {
    private Span<String> refs;

    @Before
    public void setup() {
        this.refs = IntStream.range(0, 30)
                .mapToObj(x -> IntStream.range(0, x)
                        .mapToObj(y -> 'a')
                        .map(String::valueOf)
                        .collect(Collectors.joining()))
                .collect(Span.collector());
    }

    @Test
    public void testSimple() {
        refs.pipe()
                .bi(String::length)
                .forEach((str, len) -> Assert.assertEquals(str.length(), (int) len));
    }
}
