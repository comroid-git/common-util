package org.comroid.test.common.ref;

import org.comroid.api.Named;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NamedTest {
    private Named named;

    @Before
    public void setup() {
        named = new Named() {
            @Override
            public String getName() {
                return "name";
            }

            @Override
            public String toString() {
                return "string";
            }
        };
    }

    @Test
    public void test() {
        Assert.assertEquals("name", String.format("%s", named));
        Assert.assertEquals("string", String.format("%#s", named));
    }
}
