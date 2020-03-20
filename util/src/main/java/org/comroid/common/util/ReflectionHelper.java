package org.comroid.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.annotation.Instance;
import org.comroid.common.iter.Span;

import org.jetbrains.annotations.Nullable;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

public final class ReflectionHelper {
    public static <T> T instance(Class<T> type, Object... args) throws RuntimeException, AssertionError {
        final Optional<T> optInstByField = instanceField(type);

        if (optInstByField.isPresent())
            return optInstByField.get();

        final Class<?>[] types = types(args);
        Constructor<T> constructor = findConstructor(type, types)
                .orElseThrow(() -> new AssertionError(String.format("Could not find constructor for class %s with types %s", type.getName(), Arrays.toString(types))));

        return instance(constructor, args);
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

    public static <T> Optional<T> instanceField(Class<T> type) {
        return fieldWithAnnotation(type, Instance.class)
                .stream()
                .filter(field -> typeCompat(type, field.getType()))
                .filter(field -> isStatic(field.getModifiers())
                        && isFinal(field.getModifiers())
                        && isPublic(field.getModifiers()))
                .findAny()
                .map(field -> {
                    try {
                        return (T) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new AssertionError("Cannot access field", e);
                    }
                });
    }

    public static <T> boolean typeCompat(Class<T> type, Class<?> other) {
        return type.equals(other) || type.isAssignableFrom(other);
    }

    public static Set<Field> fieldWithAnnotation(Class<?> type, Class<? extends Annotation> instanceClass) {
        Set<Field> yields = new HashSet<>();

        for (Field field : type.getFields())
            if (field.isAnnotationPresent(instanceClass))
                yields.add(field);

        return yields;
    }

    public static Class<?>[] types(Object... args) {
        final Class<?>[] yields = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            yields[i] = args[i].getClass();

        return yields;
    }

    public static <T> Span<T> collectStaticFields(Class<? extends T> fieldType,
                                                  Class<?> inClass,
                                                  boolean forceAccess,
                                                  @Nullable Class<? extends Annotation> withAnnotation) {
        final Field[] fields = inClass.getFields();
        final HashSet<T> values = new HashSet<>(fields.length);

        for (Field field : fields) {
            if (forceAccess && !field.isAccessible())
                field.setAccessible(true);

            if (isStatic(field.getModifiers())
                    && fieldType.isAssignableFrom(field.getType())
                    && (withAnnotation == null || field.isAnnotationPresent(withAnnotation))) {
                try {
                    values.add((T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("", e);
                }
            }
        }

        return Span.<T>make()
                .initialValues(values)
                .fixedSize(true)
                .span();
    }

    public static <T> Optional<Constructor<T>> findConstructor(Class<T> inClass, Class<?>[] types) {
        final Constructor<?>[] constructors = inClass.getDeclaredConstructors();

        if (constructors.length == 0)
            return Optional.empty();

        return Stream.of(constructors)
                .map(it -> (Constructor<T>) it)
                .max(Comparator.comparingLong(constr ->
                        Stream.of(constr.getParameterTypes())
                                .filter(typ -> Stream.of(types)
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

    public static boolean verifyClassDependencies(Class<?> forType) throws IllegalStateException {
        return annotation(forType, ClassDependency.class)
                .map(ClassDependency::value)
                .filter(classnames -> Stream.of(classnames)
                        .allMatch(ReflectionHelper::classExists))
                .isPresent();
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }

    private static <T extends Annotation> Optional<T> annotation(Class<?> type, Class<T> annotationType) {
        if (type.isAnnotationPresent(annotationType))
            return Optional.ofNullable(type.getAnnotation(annotationType));

        return Optional.empty();
    }
}
