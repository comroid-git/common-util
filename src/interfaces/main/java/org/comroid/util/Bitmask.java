package org.comroid.util;

import org.comroid.api.BitmaskEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;

public final class Bitmask {
    public static final int EMPTY = 0x0;
    private static final Map<Class<?>, AtomicInteger> LAST_FLAG = new ConcurrentHashMap<>();

    public static int combine(BitmaskEnum... values) {
        int yield = EMPTY;

        for (BitmaskEnum value : values)
            yield = value.apply(yield, true);

        return yield;
    }

    public static int modifyFlag(int mask, int flag, boolean newState) {
        if (isFlagSet(mask, flag) && !newState) {
            // add flag
            return mask | flag;
        } else if (!isFlagSet(mask, flag) && newState) {
            // remove flag
            return mask & ~flag;
        } else {
            return mask; // do nothing
        }
    }

    public static boolean isFlagSet(int mask, int flag) {
        return (mask & flag) != 0;
    }

    //@CallerSensitive
    public static int nextFlag() {
        return nextFlag(0);
    }

    //@CallerSensitive
    public static int nextFlag(int traceDelta) {
        final AtomicInteger atom = LAST_FLAG.computeIfAbsent(StackTraceUtils
                .callerClass(1 + traceDelta), key -> new AtomicInteger(-1));

        atom.accumulateAndGet(1, Integer::sum);
        return 1 << atom.get();
    }

    public static int combine(int... masks) {
        int yield = EMPTY;

        for (int mask : masks) {
            yield = yield | mask;
        }

        return yield;
    }

    @SafeVarargs
    public static <T> int combine(ToIntFunction<T> mapper, T... items) {
        int yield = EMPTY;

        for (T item : items) {
            yield = yield | mapper.applyAsInt(item);
        }

        return yield;
    }

}