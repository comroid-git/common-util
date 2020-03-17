package org.comroid.uniform.data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.comroid.common.annotation.ClassDependency;
import org.comroid.common.func.bi.Junction;
import org.comroid.common.util.ReflectionHelper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public abstract class SeriLib<BAS, OBJ extends BAS, ARR extends BAS> {
    protected static <T> T loadAdapter(Class<T> adapterClass) throws ExceptionInInitializerError {
        final ClassDependency classDependency = adapterClass.getAnnotation(ClassDependency.class);

        Set<String> missingClasses = new HashSet<>();
        for (String depClass : (classDependency == null ? new String[0] : classDependency.value())) {
            try {
                Class.forName(depClass);
            } catch (ClassNotFoundException ignored) {
                missingClasses.add(depClass);
            }
        }
        if (!missingClasses.isEmpty())
            throw new ExceptionInInitializerError(String.format("Missing dependency classes:%s",
                    missingClasses.stream().collect(Collectors.joining("\n\t-\t", "", "\n"))));
        else System.err.printf("Missing ClassDependency annotation on class %s\n", adapterClass.getName());

        try {
            return ReflectionHelper.instance(adapterClass);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public final Junction<String, BAS> parser;
    public final DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType;
    public final DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType;
    public final BiFunction<OBJ, String, ARR> arrayExtractor;
    private final Map<BAS, NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, ?>> dummyCache = new ConcurrentHashMap<>();

    protected SeriLib(
            Junction<String, BAS> parser,
            DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType,
            DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType,
            BiFunction<OBJ, String, ARR> arrayExtractor
    ) {
        this.parser = parser;
        this.objectType = objectType;
        this.arrayType = arrayType;
        this.arrayExtractor = arrayExtractor;
    }

    protected SeriLib(
            Junction<String, BAS> parser,
            Class<OBJ> objClass,
            Class<ARR> arrClass,
            BiFunction<OBJ, String, ARR> arrayExtractor,
            ToIntFunction<ARR> arraySizeEvaluator,
            Function<BAS, List<BAS>> arraySplitter
    ) {
        this(
                parser,
                new DataStructureType.Obj<>(objClass),
                new DataStructureType.Arr<>(arrClass, arraySizeEvaluator, arraySplitter),
                arrayExtractor
        );
    }

    @Contract("null -> null")
    public <TAR extends BAS> @Nullable NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, TAR> dummy(@Nullable TAR node) {
        if (node == null)
            return null;

        return (NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, TAR>) dummyCache
                .computeIfAbsent(node, key -> createNodeDummy(node));
    }

    @Override
    public String toString() {
        return String.format("SeriLib{lib=%s}", getClass().getName());
    }

    public <TAR extends BAS> DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> typeOf(TAR node) {
        if (objectType.typeClass().isInstance(node))
            return (DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR>) objectType;
        if (arrayType.typeClass().isInstance(node))
            return (DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR>) arrayType;

        throw new IllegalArgumentException("Unknown type: " + node.getClass().getName());
    }

    protected abstract <TAR extends BAS> NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, TAR> createNodeDummy(TAR node);

}
