package org.comroid.test.dreadpool;

import org.comroid.dreadpool.model.LoopManager;

import org.junit.Before;
import org.junit.Test;

public class LoopPoolTest {
    private LoopManager loopManager;

    @Before
    public void setup() {
        loopManager = LoopManager.start(5);
    }

    @Test
    public void test() {
    }
}
