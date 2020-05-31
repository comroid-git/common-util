package org.comroid.test.common.ref;

import org.comroid.common.func.Processor;
import org.comroid.common.ref.Reference;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;

public class ReferenceTest {
    @Test
    public void testWrap() {
        Assert.assertTrue(Reference.constant("anything").wrap().isPresent());
        Assert.assertFalse(Reference.empty().wrap().isPresent());
    }

    @Test
    public void testRequireNonNull() {
        Reference.constant("anything").requireNonNull();
    }

    @Test(expected = NullPointerException.class)
    public void testRequireNonNullThrows() {
        Reference.empty().requireNonNull();
    }

    @Test
    public void testProcessorMap() {
        Reference.Settable<String> ref = Reference.Settable.create("abc");
        Processor<String> charCodes = ref.process()
                .map(str -> str.chars()
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining("-")));

        Assert.assertEquals("abc", ref.get());
        Assert.assertEquals("97-98-99", charCodes.get());

        ref.set("def");

        Assert.assertEquals("def", ref.get());
        Assert.assertEquals("100-101-102", charCodes.get());
    }

    @Test
    public void testProcessorFilter() {
        Reference.Settable<Boolean> ref = Reference.Settable.create(false);
        Processor<Boolean> existsIfTrue = ref.process().filter(it -> it);

        Assert.assertFalse(ref.requireNonNull("Unboxing error"));
        Assert.assertTrue(existsIfTrue.isNull());

        ref.set(true);

        Assert.assertTrue(ref.requireNonNull("Unboxing error"));
        Assert.assertFalse(existsIfTrue.isNull());
    }

    @Test
    public void testComplexProcessor() {
        final Reference.Settable<String> ref = Reference.Settable.create("abc");
        final Processor<String> processor = ref.process()
                .flatMap(Reference::constant)
                .map(str -> str.chars()
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining("-")))
                .filter(str -> str.contains("-"));

        Assert.assertTrue(processor.isPresent());
        Assert.assertEquals("97-98-99", processor.get());
        Assert.assertTrue(processor.test(str -> str.contains("-")));
        Assert.assertEquals("97_98_99", processor.into(str -> str.chars()
                .mapToObj(x -> ((char) x) == '-' ? "_" : String.valueOf((char) x))
                .collect(Collectors.joining())));

        ref.set("");

        Assert.assertTrue(processor.isNull());
    }
}
