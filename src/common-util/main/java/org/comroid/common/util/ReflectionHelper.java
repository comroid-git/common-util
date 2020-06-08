package org.comroid.common.util;

import org.comroid.api.Polyfill;
import org.comroid.common.annotation.Instance;
import org.comroid.common.iter.Span;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.reflect.Modifier.*;

public final class ReflectionHelper {
    public static <T> T instance(Class<T> type, Object... args) throws RuntimeException, AssertionError {
        final Optional<T> optInstByField = instanceField(type);

        if (optInstByField.isPresent()) {
            return optInstByField.get();
        }

        final Class<?>[] types = types(args);

        return Arrays.stream(type.getConstructors())
                .filter(constr -> constr.getParameterCount() == types.length)
                .filter(constr -> {
                    final Class<?>[] params = constr.getParameterTypes();

                    for (int i = 0; i < types.length; i++)
                        if (!params[i].isAssignableFrom(types[i]))
                            return false;

                    return true;
                })
                .findAny()
                .map(constr -> instance(constr, args))
                .map(Polyfill::<T>uncheckedCast)
                .orElseThrow(() -> new NoSuchElementException("No suitable constructor found in class: " + type));
    }

    public static <T> Optional<T> instanceField(Class<T> type) {
        return fieldWithAnnotation(type, Instance.class).stream()
                .filter(field -> typeCompat(type, field.getType()))
                .filter(field -> isStatic(field.getModifiers()) && isFinal(field.getModifiers()) &&
                        isPublic(field.getModifiers()))
                .findAny()
                .map(field -> {
                    try {
                        return (T) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new AssertionError("Cannot access field", e);
                    }
                });
    }

    public static Class<?>[] types(Object... args) {
        final Class<?>[] yields = new Class[args.length];

        for (int i = 0; i < args.length; i++) {
            yields[i] = args[i].getClass();
        }

        return yields;
    }

    public static <T> Optional<Constructor<T>> findConstructor(Class<T> inClass, Class<?>[] types) {
        final Constructor<?>[] constructors = inClass.getDeclaredConstructors();

        if (constructors.length == 0) {
            return Optional.empty();
        }

        return Stream.of(constructors)
                .map(it -> (Constructor<T>) it)
                .max(Comparator.comparingLong(constr -> Stream.of(constr.getParameterTypes())
                        .filter(typ -> Stream.of(types)
                                .anyMatch(typ::isAssignableFrom))
                        .count()));
    }

    public static <T> T instance(Constructor<T> constructor, Object... args) throws RuntimeException, AssertionError {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error in Constructor", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(String.format("Could not access constructor %s", constructor), e);
        } catch (InstantiationException e) {
            throw new AssertionError(String.format("Class %s is abstract",
                    constructor.getDeclaringClass()
                            .getName()
            ), e);
        }
    }

    public static Set<Field> fieldWithAnnotation(
            Class<?> type, Class<? extends Annotation> instanceClass
    ) {
        Set<Field> yields = new HashSet<>();

        for (Field field : type.getFields()) {
            if (field.isAnnotationPresent(instanceClass)) {
                yields.add(field);
            }
        }

        return yields;
    }

    public static <T> boolean typeCompat(Class<T> type, Class<?> other) {
        return type.equals(other) || type.isAssignableFrom(other);
    }

    public static <T> Span<T> collectStaticFields(
            Class<? extends T> fieldType,
            Class<?> inClass,
            boolean forceAccess,
            @Nullable Class<? extends Annotation> withAnnotation
    ) {
        final Field[] fields = inClass.getFields();
        final HashSet<T> values = new HashSet<>(fields.length);

        for (Field field : fields) {
            if (forceAccess && !field.isAccessible()) {
                field.setAccessible(true);
            }

            if (isStatic(field.getModifiers())
                    && fieldType.isAssignableFrom(field.getType())
                    && (withAnnotation == null || field.isAnnotationPresent(withAnnotation))) {
                try {
                    values.add(fieldType.cast(field.get(null)));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("", e);
                }
            }
        }

        return Span.immutable(values);
    }

    public static Object[] arrange(Object[] args, Class<?>[] typesOrdered) {
        final Object[] yields = new Object[typesOrdered.length];

        for (int i = 0; i < typesOrdered.length; i++) {
            int finalli = i;
            yields[i] = Stream.of(args)
                    .filter(it -> typesOrdered[finalli].isInstance(it))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "No instance of " + typesOrdered[finalli].getName() + " found in array"));
        }

        return yields;
    }

    public static <T> Class<? super T> canonicalClass(Class<T> of) {
        if (Object.class.equals(of) || Void.class.equals(of)) {
            return Object.class;
        }
        if (Modifier.isInterface(of.getModifiers()) || of.isPrimitive()) {
            return of;
        }
        if (of.isAnonymousClass()) {
            return canonicalClass(of.getSuperclass());
        }

        return of;
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Class<A> annotation, Class<?> inClass, ElementType target) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (target) {
            case TYPE:
                return StreamSupport.stream(recursiveClassGenerator(inClass), false)
                        .filter(type -> type.isAnnotationPresent(annotation))
                        .findFirst()
                        .map(type -> type.getAnnotation(annotation));
            default:
                throw new UnsupportedOperationException("Please contact the developer");
        }
    }

    private static Spliterator<Class<?>> recursiveClassGenerator(Class<?> from) {
        return Spliterators.spliteratorUnknownSize(new Iterator<Class<?>>() {
            private final Queue<Class<?>> queue = new LinkedBlockingQueue<>();

            {
                queue.add(from);
            }

            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public Class<?> next() {
                if (hasNext()) {
                    Class<?> poll = queue.poll();
                    assert poll != null;

                    Optional.ofNullable(poll.getSuperclass())
                            .ifPresent(queue::add);
                    queue.addAll(Arrays.asList(poll.getInterfaces()));
                    return poll;
                } else {
                    throw new IndexOutOfBoundsException("No more classes available!");
                }
            }
        }, Spliterator.DISTINCT);
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }

    private static <T extends Annotation> Optional<T> annotation(
            Class<?> type, Class<T> annotationType
    ) {
        if (type.isAnnotationPresent(annotationType)) {
            return Optional.ofNullable(type.getAnnotation(annotationType));
        }

        return Optional.empty();
    }

    public static Stream<Method> externalMethodsAbove(Class<?> above, Class<?> startingFrom) {
        return Arrays.stream(above.getMethods())
                .filter(mtd -> !mtd.getDeclaringClass().isAssignableFrom(above));
    }
}
