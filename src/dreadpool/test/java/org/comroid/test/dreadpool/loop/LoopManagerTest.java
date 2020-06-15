package org.comroid.test.dreadpool.loop;

import org.comroid.dreadpool.loop.WhileDo;
import org.comroid.dreadpool.loop.manager.Loop;
import org.comroid.dreadpool.loop.manager.LoopManager;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopManagerTest {
    private final List<String> results = new ArrayList<>();
    private final Loop<Integer> lowPrioLoop1 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("low-while1-#2");
        }

        @Override
        protected boolean executeLoop(Integer each) {
            return results.add("low-while1-#" + each);
        }
    };
    private final Loop<Integer> lowPrioLoop2 = new WhileDo<Integer>(Loop.LOW_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("low-while2-#2");
        }

        @Override
        protected boolean executeLoop(Integer each) {
            return results.add("low-while2-#" + each);
        }
    };
    private final Loop<Integer> medPrioLoop1 = new WhileDo<Integer>(Loop.MEDIUM_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("med-while1-#2");
        }

        @Override
        protected boolean executeLoop(Integer each) {
            return results.add("med-while1-#" + each);
        }
    };
    private final Loop<Integer> medPrioLoop2 = new WhileDo<Integer>(Loop.MEDIUM_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("med-while2-#2");
        }

        @Override
        protected boolean executeLoop(Integer each) {
            return results.add("med-while2-#" + each);
        }
    };
    private final Loop<Integer> higPrioLoop1 = new WhileDo<Integer>(Loop.HIGH_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("hig-while1-#2");
        }

        @Override
        protected boolean executeLoop(Integer each) {
            return results.add("hig-while1-#" + each);
        }
    };
    private final Loop<Integer> higPrioLoop2 = new WhileDo<Integer>(Loop.HIGH_PRIO, val -> val + 1) {
        @Override
        protected boolean continueLoop() {
            return !results.contains("hig-while2-#2");
        }

        @Override
        protected boolean executeLoop(Integer each) {
            return results.add("hig-while2-#" + each);
        }
    };
    private LoopManager loopManager;

    @Before
    public void setup() {
        loopManager = LoopManager.start(1);
    }

    @Test
    public void test() {
        loopManager.queue(lowPrioLoop1);
        loopManager.queue(higPrioLoop1);
        loopManager.queue(medPrioLoop1);
        loopManager.queue(higPrioLoop2);
        loopManager.queue(medPrioLoop2);
        loopManager.queue(lowPrioLoop2);

        CompletableFuture.allOf(
                lowPrioLoop1.result,
                lowPrioLoop2.result,
                medPrioLoop1.result,
                medPrioLoop2.result,
                higPrioLoop1.result,
                higPrioLoop2.result
        )
                .join();

        System.out.println("Results:");
        results.forEach(System.out::println);

        assertEquals(12, results.size());

        assertTrue(results.stream()
                .limit(4)
                .allMatch(str -> str.startsWith("hig")));
        assertTrue(results.stream()
                .skip(4)
                .limit(4)
                .allMatch(str -> str.startsWith("med")));
        assertTrue(results.stream()
                .skip(8)
                .limit(4)
                .allMatch(str -> str.startsWith("low")));
    }
}
