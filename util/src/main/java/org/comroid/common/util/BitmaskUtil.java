package org.comroid.common.util;

import jdk.internal.reflect.CallerSensitive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;

public final class BitmaskUtil {
    public static final  int                          EMPTY     = 0x0;
    private static final Map<Class<?>, AtomicInteger> LAST_FLAG = new ConcurrentHashMap<>();

    public static boolean isFlagSet(int mask, int flag) {
        return (mask & flag) != 0;
    }

    public static int modifyFlag(int mask, int flag, boolean newState) {
        if (isFlagSet(mask, flag) && !newState) {
            // add flag
            return mask | flag;
        } else if (!isFlagSet(mask, flag) && newState) {
            // remove flag
            return mask & ~flag;
        } else
            return mask; // do nothing
    }

    @CallerSensitive
    public static int nextFlag() {
        return LAST_FLAG.computeIfAbsent(StackTraceUtils.callerClass(1), key -> new AtomicInteger(0))
                .updateAndGet(value -> {
                    if (value <= 0)
                        return 1;

                    return value == 1 ? 2 : (int) Math.pow(value, 2);
                });
    }

    public static Collector<AtomicInteger, AtomicInteger, Integer> collectMask() {
        return Collector.of(
                () -> new AtomicInteger(0),
                (left, right) -> left.updateAndGet(value -> combine(value, right.get())),
                (left, right) -> {
                    left.updateAndGet(value -> combine(value, right.get()));

                    return left;
                },
                AtomicInteger::get,
                Collector.Characteristics.IDENTITY_FINISH,
                Collector.Characteristics.CONCURRENT
        );
    }

    public static int combine(int... masks) {
        int yield = EMPTY;

        for (int mask : masks)
            yield = yield | mask;

        return yield;
    }
}
