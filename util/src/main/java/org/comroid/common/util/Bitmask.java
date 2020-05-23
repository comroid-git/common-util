package org.comroid.common.util;

import org.comroid.common.ref.IntEnum;
import org.comroid.common.ref.SelfDeclared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class Bitmask {
    public static final int EMPTY = 0x0;
    private static final Map<Class<?>, AtomicInteger> LAST_FLAG = new ConcurrentHashMap<>();

    public static int combine(Bitmask.Enum... values) {
        int yield = EMPTY;

        for (Bitmask.Enum value : values)
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

    public interface Enum<S extends Enum<S>> extends IntEnum, SelfDeclared<S> {
        @Override
        int getValue();

        static <T extends java.lang.Enum<? extends T> & Enum<T>> Set<T> valueOf(int mask, Class<T> viaEnum) {
            return valueOf(mask, viaEnum, Class::getEnumConstants);
        }

        static <T extends java.lang.Enum<? extends T> & Enum<T>> Set<T> valueOf(
                int mask,
                Class<T> viaEnum,
                Function<Class<T>, T[]> valuesProvider) {
            if (!viaEnum.isEnum())
                throw new IllegalArgumentException("Only enums allowed as parameter 'viaEnum'");

            final T[] constants = valuesProvider.apply(viaEnum);
            HashSet<T> yields = new HashSet<>();

            for (T constant : constants) {
                if (constant.isFlagSet(mask))
                    yields.add(constant);
            }

            return Collections.unmodifiableSet(yields);
        }

        default boolean hasFlag(Enum<S> other) {
            return Bitmask.isFlagSet(getValue(), other.getValue());
        }

        default boolean isFlagSet(int inMask) {
            return Bitmask.isFlagSet(inMask, getValue());
        }

        default int apply(int toMask, boolean newState) {
            return Bitmask.modifyFlag(toMask, getValue(), newState);
        }
    }
}
