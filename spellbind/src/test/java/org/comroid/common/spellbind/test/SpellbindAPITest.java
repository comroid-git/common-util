package org.comroid.common.spellbind.test;

import org.comroid.common.spellbind.Spellbind;
import de.kaleidox.spellbind.test.model.FullAbstract;
import de.kaleidox.spellbind.test.model.NonAbstract;
import de.kaleidox.spellbind.test.model.PartialAbstract;

import org.junit.Test;

public class SpellbindAPITest {
    @Test
    public void testAPI() {
        final ImplementingClass implementingClass = new ImplementingClass();

        MainInterface proxy = Spellbind.builder(MainInterface.class)
                .coreObject(implementingClass)
                .build();

        System.out.println("proxy.cast(Integer.class) = " + proxy.cast(Integer.class));
        System.out.println("proxy.string() = " + proxy.string());
        System.out.println("proxy.add(5) = " + proxy.add(5));
        System.out.println("proxy.getValue() = " + proxy.getValue());
        System.out.println("proxy.name() = " + proxy.name());
        System.out.println("proxy.invert(true) = " + proxy.invert(true));
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
