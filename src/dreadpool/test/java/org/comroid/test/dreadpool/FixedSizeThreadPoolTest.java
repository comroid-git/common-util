package org.comroid.test.dreadpool;

import org.comroid.api.UUIDContainer;
import org.comroid.dreadpool.ThreadPool;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class FixedSizeThreadPoolTest {
    private ThreadPool threadPool;
    private List<SomeTask> someTasks;
    private List<UUID> yields;

    @Before
    public void setup() {
        threadPool = ThreadPool.fixedSize(null, 1);

        someTasks = new ArrayList<>();
        IntStream.range(0, 50)
                .forEach(nil -> someTasks.add(new SomeTask()));
        yields = new ArrayList<>();
    }

    @Test
    public void test() throws InterruptedException {
        someTasks.stream()
                .limit(10)
                .sequential()
                .forEachOrdered(threadPool::execute);
        assertEquals(0, threadPool.queueSize());
        threadPool.flush();
        Thread.sleep(200);
        assertEquals(10, yields.size());

        ArrayDeque<Runnable> reverse1 = someTasks.stream()
                .skip(10)
                .limit(10)
                .sequential()
                .collect(Collector.of(ArrayDeque::new, ArrayDeque::addFirst, (d1, d2) -> {
                    d2.addAll(d1);
                    return d2;
                }));
        reverse1.forEach(threadPool::queue);
        assertEquals(10, threadPool.queueSize());
        threadPool.flush();
        Thread.sleep(200);
        assertEquals(20, yields.size());

        List<Long> added = someTasks.stream()
                .skip(20)
                .limit(10)
                .sequential()
                .map(threadPool::queue)
                .collect(Collectors.toList());
        assertEquals(10, threadPool.queueSize());
        added.stream()
                .sorted(Comparator.reverseOrder())
                .limit(5)
                .sequential()
                .forEachOrdered(threadPool::unqueue);
        assertEquals(5, threadPool.queueSize());
        threadPool.flush();
        Thread.sleep(200);
        assertEquals(25, yields.size());

        ArrayDeque<Runnable> reverse2 = someTasks.stream()
                .skip(30)
                .limit(10)
                .sequential()
                .collect(Collector.of(ArrayDeque::new, ArrayDeque::addFirst, (d1, d2) -> {
                    d2.addAll(d1);
                    return d2;
                }));
        reverse2.forEach(threadPool::execute);
        assertEquals(0, threadPool.queueSize());
        threadPool.flush();
        Thread.sleep(200);
        assertEquals(35, yields.size());

        someTasks.stream()
                .skip(40)
                .limit(10)
                .sequential()
                .forEachOrdered(threadPool::queue);
        assertEquals(10, threadPool.queueSize());
        threadPool.flush();
        Thread.sleep(200);
        assertEquals(45, yields.size());

        final UUID[] array = yields.toArray(new UUID[0]);
        assertEquals(45, array.length);

        UUID[] arr1 = Arrays.copyOfRange(array, 0, 10);
        for (int i = 0; i < arr1.length; i++) {
            assertEquals("index: " + i, someTasks.get(i).getUUID(), arr1[i]);
        }

        List<UUID> reversed1 = Arrays.asList(Arrays.copyOfRange(array, 10, 20));
        reversed1.sort(Comparator.reverseOrder());
        UUID[] arr2 = reversed1.toArray(new UUID[0]); // reversed
        for (int i = 0; i < arr2.length; i++) {
            assertEquals("index: " + i, someTasks.get(i + 10).getUUID(), arr2[i]);
        }
        // todo: fails probably due to a concurrency problem

        UUID[] arr3 = Arrays.copyOfRange(array, 20, 25); // orig ind[20-30;first half]
        for (int i = 0; i < arr3.length; i++) {
            assertEquals("index: " + i, someTasks.get(i + 20).getUUID(), arr3[i]);
        }

        List<UUID> reversed2 = Arrays.asList(Arrays.copyOfRange(array, 25, 35));
        reversed2.sort(Comparator.reverseOrder());
        UUID[] arr4 = reversed2.toArray(new UUID[0]); // reversed
        for (int i = 0; i < arr4.length; i++) {
            assertEquals("index: " + i, someTasks.get(i + 25).getUUID(), arr4[i]);
        }

        UUID[] arr5 = Arrays.copyOfRange(array, 35, 45);
        for (int i = 0; i < arr5.length; i++) {
            assertEquals("index: " + i, someTasks.get(i + 35).getUUID(), arr5[i]);
        }
    }

    public class SomeTask extends UUIDContainer implements Runnable {
        @Override
        public void run() {
            System.out.printf("Running Task %s", getUUID());
            yields.add(getUUID());
        }
    }
}
