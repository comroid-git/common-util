package org.comroid.common.spellbind.test;

import org.comroid.common.spellbind.Spellbind;
import org.comroid.test.model.FullAbstract;
import org.comroid.test.model.NonAbstract;
import org.comroid.test.model.PartialAbstract;

import org.junit.Assert;
import org.junit.Test;

public class SpellbindTest {
    @Test
    public void testDirectBound() {
        final ImplementingClass implementingClass = new ImplementingClass();

        MainInterface proxy = Spellbind.builder(MainInterface.class)
                .coreObject(implementingClass)
                .build();

        Assert.assertTrue(proxy.cast(PartialAbstract.class).isPresent());
        Assert.assertFalse(proxy.string().isPresent());

        Assert.assertEquals(47, proxy.add(5));
        Assert.assertEquals(42, proxy.getValue());

        Assert.assertEquals("some class", proxy.name());
        Assert.assertFalse(proxy.invert(true));
    }

    @Test
    public void testMultipleBound() {
        final ImplementingClass implementingClass = new ImplementingClass();

        HyperInterface proxy = Spellbind.builder(HyperInterface.class)
                .coreObject(implementingClass)
                .subImplement(new HyperInterface.SubImpl(), CharSequence.class)
                .build();

        Assert.assertTrue(proxy.cast(PartialAbstract.class).isPresent());
        Assert.assertFalse(proxy.string().isPresent());

        Assert.assertEquals(47, proxy.add(5));
        Assert.assertEquals(42, proxy.getValue());

        Assert.assertEquals("some class", proxy.name());
        Assert.assertFalse(proxy.invert(true));

        Assert.assertTrue(proxy instanceof CharSequence);
        Assert.assertEquals(5, proxy.length());
        Assert.assertEquals('e', proxy.charAt(1));
        Assert.assertEquals('l', proxy.charAt(3));
        Assert.assertEquals("ello", proxy.subSequence(1, 5));
    }

    public interface HyperInterface extends MainInterface, CharSequence {
        @SuppressWarnings("NullableProblems")
        class SubImpl extends ImplementingClass implements HyperInterface {
            @Override
            public int length() {
                return 5;
            }

            @Override
            public char charAt(int index) {
                return "hello".charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return "hello".subSequence(start, end);
            }
        }
    }

    public interface MainInterface extends NonAbstract, PartialAbstract, FullAbstract {
    }

    public static class ImplementingClass implements MainInterface {
        @Override
        public boolean invert(boolean val) {
            return !val;
        }

        @Override
        public String name() {
            return "some class";
        }

        @Override
        public int getValue() {
            return 42;
        }
    }
}
