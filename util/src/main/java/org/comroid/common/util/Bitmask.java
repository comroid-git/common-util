package org.comroid.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;

public final class Bitmask {
    public static final int EMPTY = 0x0;

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
        return LAST_FLAG.computeIfAbsent(StackTraceUtils.callerClass(1 + traceDelta), key -> new AtomicInteger(0))
                .getAndUpdate(value -> {
                    if (value == 3) {
                        throw new RuntimeException("Too many Flags requested! Integer Overflow");
                    }

                    if (value < 0 && value * 2 != 0) {
                        return value == -1 ? -2 : value * 2;
                    } else if (value < 0) {
                        return 3;
                    }

                    if (value == 0) {
                        return 1;
                    }
                    if (value * 2 == Integer.MIN_VALUE) {
                        return -1;
                    }

                    return value == 1 ? 2 : value * 2;
                });
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

    private static final Map<Class<?>, AtomicInteger> LAST_FLAG = new ConcurrentHashMap<>();
}
