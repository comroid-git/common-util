package org.comroid.common.util;

import jdk.internal.reflect.CallerSensitive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class BitmaskUtil {
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
}
