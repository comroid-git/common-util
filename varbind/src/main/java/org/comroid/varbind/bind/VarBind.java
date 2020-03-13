package org.comroid.varbind.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

public class VarBind<T, C, BAS, TAR extends BAS> implements GroupedBind {
    private final @Nullable GroupBind group;
    private final String name;
    private final Function<C, T> finisher;
    private final BiFunction<TAR, String, C> extractor;

    public VarBind(@Nullable GroupBind group, String name, BiFunction<TAR, String, C> extractor, Function<C, T> finisher) {
        this.group = group;
        this.name = name;
        this.finisher = finisher;
        this.extractor = extractor;
    }

    public T finish(C from) {
        return finisher.apply(from);
    }

    public C extract(TAR node) {
        return extractor.apply(node, name);
    }

    @Override
    public Optional<GroupBind> getGroup() {
        return Optional.ofNullable(group);
    }

    public final String name() {
        return name;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Location {
        Class<?> value();
    }

    public static class Uno<T> extends VarBind<T, T, Object, Object> {
        public Uno(@Nullable GroupBind group, String name, BiFunction<Object, String, T> extractor, Function<T, T> finisher) {
            super(group, name, extractor, finisher);
        }
    }

    public static class Duo<T, C> extends VarBind<T, C, Object, Object> {
        public Duo(@Nullable GroupBind group, String name, BiFunction<Object, String, C> extractor, Function<C, T> finisher) {
            super(group, name, extractor, finisher);
        }
    }

    public static class Dep<T, C, D> extends VarBind<T, C, Object, Object> {
        private final BiFunction<D, C, T> resolver;

        public Dep(@Nullable GroupBind group, String name, BiFunction<Object, String, C> extractor, BiFunction<D, C, T> resolver) {
            super(group, name, extractor, c -> {
                throw new AssertionError("Unexpected Call");
            });

            this.resolver = resolver;
        }

        public T finish(D dependency, C from) {
            return resolver.apply(dependency, from);
        }
    }
}
