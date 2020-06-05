package org.comroid.common.iter;

import java.util.function.IntUnaryOperator;

public final class Operator {
    public static IntUnaryOperator intOrder(int... values) {
        class Ordered implements IntUnaryOperator {
            private final int[] arr = values;

            @Override
            public int applyAsInt(int operand) {
                if (i < arr.length) {
                    return arr[i++];
                }

                return -1;
            }

            private int i = 0;
        }

        return new Ordered();
    }
}
