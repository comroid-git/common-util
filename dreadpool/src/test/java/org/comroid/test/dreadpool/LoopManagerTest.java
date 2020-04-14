package org.comroid.test.dreadpool;

import java.util.ArrayList;
import java.util.List;

import org.comroid.dreadpool.loop.ForI;
import org.comroid.dreadpool.loop.WhileDo;
import org.comroid.dreadpool.model.Loop;
import org.comroid.dreadpool.model.LoopManager;

import org.junit.Before;
import org.junit.Test;

public class LoopManagerTest {
    private LoopManager loopManager;

    private final List<String> results = new ArrayList<>();

    private final Loop<Integer> lowPrioLoop1 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean canContinue() {
            return !results.contains("low-while-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("low-while-#" + each);
        }
    };
    private Loop<Integer> lowPrioLoop2 = new ForI<Integer>(Loop.LOW_PRIO) {
        @Override
        protected void execute(Integer each) {

        }

        @Override
        protected Integer init() {
            return null;
        }

        @Override
        protected boolean canContinueWith(Integer value) {
            return false;
        }

        @Override
        protected Integer accumulate(Integer value) {
            return null;
        }
    };
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
        loopManager.queue(lowPrioLoop1);
        loopManager.queue(medPrioLoop1);
        loopManager.queue(higPrioLoop1);
        loopManager.queue(higPrioLoop2);
        loopManager.queue(medPrioLoop2);
        loopManager.queue(lowPrioLoop2);
    }
}
