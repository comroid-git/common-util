package org.comroid.test.dreadpool;

import org.comroid.dreadpool.loop.WhileDo;
import org.comroid.dreadpool.model.Loop;
import org.comroid.dreadpool.model.LoopManager;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LoopManagerTest {
    private final List<String> results = new ArrayList<>();
    private final Loop<Integer> lowPrioLoop1 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("low-while1-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("low-while1-#" + each);
        }
    };
    private LoopManager loopManager;
    private Loop<Integer> lowPrioLoop2 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("low-while2-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("low-while2-#" + each);
        }
    };
    private Loop<Integer> medPrioLoop1 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("med-while1-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("med-while1-#" + each);
        }
    };
    private Loop<Integer> medPrioLoop2 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("med-while2-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("med-while2-#" + each);
        }
    };
    private Loop<Integer> higPrioLoop1 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("hig-while1-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("hig-while1-#" + each);
        }
    };
    private Loop<Integer> higPrioLoop2 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("hig-while2-#2");
        }

        @Override
        protected void execute(Integer each) {
            results.add("hig-while2-#" + each);
        }
    };

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

        results.forEach(System.out::println);
    }
}
