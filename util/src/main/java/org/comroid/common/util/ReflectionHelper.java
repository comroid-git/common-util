package org.comroid.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ReflectionHelper {
    public static <T> T instance(Class<T> type, Object... args) throws RuntimeException, AssertionError {
        final Class<?>[] types = types(args);
        Constructor<T> constructor = null;

        try {
            constructor = type.getConstructor(types);

            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error in Constructor", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(String.format("Could not access constructor %s", constructor), e);
        } catch (InstantiationException e) {
            throw new AssertionError(String.format("Class %s is abstract", type.getName()), e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(String.format("Could not find constructor with types %s", Arrays.toString(types)), e);
        }
    }

    public static Class<?>[] types(Object... args) {
        final Class<?>[] yields = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            yields[i] = args[i].getClass();

        return yields;
    }

    public static <T> Set<T> collectStaticFields(Class<? extends T> fieldType, Class<?> inClass) {
        final Field[] fields = inClass.getFields();
        final HashSet<T> values = new HashSet<>(fields.length);

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.isAccessible() && fieldType.isAssignableFrom(field.getType())) {
                try {
                    values.add((T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("", e);
                }
            }
        }

        return Collections.unmodifiableSet(values);
    }
}
