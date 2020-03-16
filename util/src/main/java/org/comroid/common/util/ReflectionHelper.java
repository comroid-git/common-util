package org.comroid.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.comroid.common.iter.Span;

import org.jetbrains.annotations.Nullable;

public final class ReflectionHelper {
    public static <T> T instance(Class<T> type, Object... args) throws RuntimeException, AssertionError {
        final Class<?>[] types = types(args);
        Constructor<T> constructor = null;

        try {
            return instance(type.getConstructor(types), args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(String.format("Could not find constructor with types %s", Arrays.toString(types)), e);
        }
    }

    public static <T> T instance(Constructor<T> constructor, Object... args) throws RuntimeException, AssertionError {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error in Constructor", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(String.format("Could not access constructor %s", constructor), e);
        } catch (InstantiationException e) {
            throw new AssertionError(String.format("Class %s is abstract", constructor.getDeclaringClass().getName()), e);
        }
    }

    public static Class<?>[] types(Object... args) {
        final Class<?>[] yields = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            yields[i] = args[i].getClass();

        return yields;
    }

    public static <T> Span<T> collectStaticFields(Class<? extends T> fieldType,
                                                  Class<?> inClass,
                                                  @Nullable Class<? extends Annotation> withAnnotation) {
        final Field[] fields = inClass.getFields();
        final HashSet<T> values = new HashSet<>(fields.length);

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.isAccessible()
                    && fieldType.isAssignableFrom(field.getType())
                    && (withAnnotation == null || field.isAnnotationPresent(withAnnotation))) {
                try {
                    values.add((T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("", e);
                }
            }
        }

        return Span.fixedSize(values);
    }

    public static <T> Optional<Constructor<T>> findConstructor(Class<T> inClass, Class<?>[] classes) {
        final Constructor<?>[] constructors = inClass.getConstructors();

        if (constructors.length == 0)
            return Optional.empty();

        return Stream.of(constructors)
                .map(it -> (Constructor<T>) it)
                .max(Comparator.comparingLong(constr ->
                        Stream.of(constr.getParameterTypes())
                                .filter(typ -> Stream.of(classes)
                                        .anyMatch(typ::isAssignableFrom))
                                .count()));
    }

    public static Object[] arrange(Object[] args, Class[] typesOrdered) {
        final Object[] yields = new Object[args.length];

        for (int i = 0; i < typesOrdered.length; i++) {
            int finalli = i;
            yields[i] = Stream.of(args)
                    .filter(it -> typesOrdered[finalli].isInstance(it))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No instance of " + typesOrdered[finalli].getName() + " found in array"));
        }

        return yields;
    }
}
