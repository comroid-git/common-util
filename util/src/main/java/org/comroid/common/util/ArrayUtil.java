package org.comroid.common.util;

import java.util.Arrays;

public final class ArrayUtil {
    public static void main(String[] args) {
        Integer[] x = new Integer[]{0,1,2,3};
        Integer[] y = new Integer[]{9,8,7};

        Integer[] yield = ArrayUtil.insert(x, 2, y);

        System.out.println("yield = " + Arrays.toString(yield));
    }

    @SafeVarargs
    public static <T> T[] insert(T[] original, int atIndex, T... insert) {
        final T[] yield = Arrays.copyOf(original, original.length + insert.length);

        for (int i, f, o = i = f = 0; i < yield.length; i++) {
            if (i >= atIndex && o < insert.length)
                yield[i] = insert[o++];
            else yield[i] = original[f++];
        }

        return yield;
    }
}
