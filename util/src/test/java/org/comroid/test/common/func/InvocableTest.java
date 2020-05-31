package org.comroid.test.common.func;

import org.comroid.common.func.Invocable;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

public final class InvocableTest {
    @Test
    public void testMagic() {
        final Invocable<String> invocable = new Invocable.Magic<String>() {
            public String doSomething(String str, int x) {
                return str + x;
            }
        };

        Assert.assertEquals("2 + 3 = 5", invocable.autoInvoke(5, "2 + 3 = "));
    }

    @Test(expected = NoSuchElementException.class)
    public void testMagicNoMethod() {
        new Invocable.Magic<String>() {
        };
    }
}
