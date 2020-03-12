package org.comroid.uniform.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.comroid.common.func.bi.Junction;
import org.comroid.common.util.ReflectionHelper;

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
    public final DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ> objectType;
    public final DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, ARR> arrayType;
    private final Map<BAS, NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, ?>> dummyCache = new ConcurrentHashMap<>();

    protected SeriLib(
            Junction<String, BAS> parser,
            DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ> objectType,
            DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, ARR> arrayType
    ) {
        this.parser = parser;
        this.objectType = objectType;
        this.arrayType = arrayType;
    }

    protected SeriLib(
            Junction<String, BAS> parser,
            Class<OBJ> objClass,
            Class<ARR> arrClass
    ) {
        this(parser, new DataStructureType.Obj<>(objClass), new DataStructureType.Arr<>(arrClass));
    }

    public <TAR extends BAS> NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, TAR> dummy(TAR node) {
        //noinspection unchecked -> cache instance problem
        return (NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, TAR>) dummyCache.computeIfAbsent(node, key -> new NodeDummy<>(this, node, typeOf(node)));
    }

    public <TAR extends BAS> DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> typeOf(TAR node) {
        if (objectType.typeClass().isInstance(node))
            //noinspection unchecked
            return (DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR>) objectType;
        if (arrayType.typeClass().isInstance(node))
            //noinspection unchecked
            return (DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR>) arrayType;

        throw new IllegalArgumentException("Unknown type: " + node.getClass().getName());
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ClassDependency {
        String[] value();
    }
}
