package org.comroid.test.dreadpool;

import org.comroid.common.iter.Span;
import org.comroid.dreadpool.ThreadPool;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class FixedSizeThreadPoolTest {
    private ThreadPool     threadPool;
    private List<SomeTask> someTasks;
    private Span<UUID>     yields;

    @Before
    public void setup() {
        threadPool = ThreadPool.fixedSize(null, 1);

        someTasks = new ArrayList<>();
        IntStream.range(0, 50)
                .forEach(nil -> someTasks.add(new SomeTask()));
        yields = new Span<>();
    }

    @Test
    public void test() {
        someTasks.stream()
                .limit(10)
                .sequential()
                .forEachOrdered(threadPool::execute);
        assertEquals(0, threadPool.queueSize());
        threadPool.flush();

        someTasks.stream()
                .skip(10)
                .limit(10)
                .sequential()
                .sorted((Comparator<? super SomeTask>) (Object) Comparator.reverseOrder())
                .forEachOrdered(threadPool::queue);
        assertEquals(10, threadPool.queueSize());
        threadPool.flush();

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

        someTasks.stream()
                .skip(30)
                .limit(10)
                .sequential()
                .sorted((Comparator<? super SomeTask>) (Object) Comparator.reverseOrder())
                .forEachOrdered(threadPool::execute);
        assertEquals(0, threadPool.queueSize());
        threadPool.flush();

        someTasks.stream()
                .skip(40)
                .limit(10)
                .sequential()
                .forEachOrdered(threadPool::queue);
        assertEquals(10, threadPool.queueSize());
        threadPool.flush();

        final UUID[] array = yields.toArray(new UUID[0]);
        assertEquals(45, array.length);

        UUID[] arr1 = Arrays.copyOfRange(array, 0, 10);
        for (int i = 0; i < arr1.length; i++)
            assertEquals(someTasks.get(i).uuid, arr1[i]);

        UUID[] arr2 = Arrays.copyOfRange(array, 10, 20); // reversed
        for (int i = 0; i < arr2.length; i++)
            assertEquals(someTasks.get(20 - i).uuid, arr2[i]);

        UUID[] arr3 = Arrays.copyOfRange(array, 20, 25); // orig ind[20-30;first half]
        for (int i = 0; i < arr3.length; i++)
            assertEquals(someTasks.get(i + 20).uuid, arr3[i]);

        UUID[] arr4 = Arrays.copyOfRange(array, 25, 35); // reversed
        for (int i = 0; i < arr4.length; i++)
            assertEquals(someTasks.get(35 - i).uuid, arr4[i]);

        UUID[] arr5 = Arrays.copyOfRange(array, 35, 45);
        for (int i = 0; i < arr5.length; i++)
            assertEquals(someTasks.get(i + 35).uuid, arr5[i]);
    }

    public class SomeTask implements Runnable {
        final UUID uuid = UUID.randomUUID();

        public UUID getUuid() {
            return uuid;
        }

        @Override
        public void run() {
            yields.add(uuid);
        }
    }
}
