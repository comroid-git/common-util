package org.comroid.common.spellbind.test;

import org.comroid.common.spellbind.Spellbind;
import org.comroid.test.model.FullAbstract;
import org.comroid.test.model.NonAbstract;
import org.comroid.test.model.PartialAbstract;

import org.junit.Assert;
import org.junit.Test;

public class SpellbindAPITest {
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
