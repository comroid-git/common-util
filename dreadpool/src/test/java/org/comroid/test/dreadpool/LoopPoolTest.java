package org.comroid.test.dreadpool;

import java.util.ArrayList;
import java.util.List;

import org.comroid.dreadpool.model.Loop;
import org.comroid.dreadpool.model.LoopManager;

import org.junit.Before;
import org.junit.Test;

public class LoopPoolTest {
    private LoopManager loopManager;

    private List<String> results = new ArrayList<>();

    private Loop<Integer> lowPrioLoop1;
    private Loop<Integer> lowPrioLoop2;
    private Loop<Integer> medPrioLoop1;
    private Loop<Integer> medPrioLoop2;
    private Loop<Integer> higPrioLoop1;
    private Loop<Integer> higPrioLoop2;

    @Before
    public void setup() {
        loopManager = LoopManager.start(1);
    }

    @Test
    public void test() {
        loopManager.
    }
}
