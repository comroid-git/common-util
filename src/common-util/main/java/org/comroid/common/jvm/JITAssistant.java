package org.comroid.common.jvm;

import org.comroid.api.Invocable;
import org.comroid.common.util.ReflectionHelper;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class JITAssistant {
    public static CompletableFuture<Void> prepare(Class<?>... classes) {
        return CompletableFuture.allOf(Arrays.stream(classes)
                .map(type -> {
                    Object instance;

                    try {
                        instance = ReflectionHelper.instance(type);
                    } catch (Throwable t) {
                        instance = null;
                    }

                    return prepareInstance(instance, type);
                })
                .toArray(CompletableFuture[]::new));
    }

    public static CompletableFuture<Void> prepareInstance(Object target, Class<?> klaas) {
        return CompletableFuture.supplyAsync(() -> {
            final Stream<Invocable<Object>> methods = Arrays.stream(klaas.getMethods())
                    .filter(method -> target != null || Modifier.isStatic(method.getModifiers()))
                    .map(method -> Invocable.ofMethodCall(target, method));
            final Stream<Invocable<Object>> fields = Arrays.stream(klaas.getFields())
                    .filter(field -> target != null || Modifier.isStatic(field.getModifiers()))
                    .map(field -> Invocable.ofCallable(() -> field.get(target)));

            Stream.concat(methods, fields).forEach(Invocable::silentAutoInvoke);

            return null;
        });
    }
}
