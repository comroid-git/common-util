package org.comroid.test.matrix;

import org.comroid.matrix.Matrix2;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class Matrix2Test {
    private final Matrix2<Long, Long, String> matrix = Matrix2.create();
    private List<Long> longs = new ArrayList<>();

    @Test
    public void setup() {
        IntStream.range(0, 50)
                .mapToLong(val -> System.currentTimeMillis())
                .forEach(val -> longs.add(val));
    }

    @Test
    public void testSetting() {
        final Iterator<Long> iter = longs.iterator();

        while (iter.hasNext()) {
            long x = iter.next();
            long y = iter.next();
            String v = x + "-" + y;

            matrix.put(x, y, v);
        }

        matrix.forEach(entry -> Assert.assertEquals(entry.getCoordinate(), String.format("%d-%d", entry.getX(), entry.getY())));
    }
}
