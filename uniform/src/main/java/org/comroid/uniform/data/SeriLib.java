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
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public abstract class SeriLib<BAS, OBJ extends BAS, ARR extends BAS> {
    protected static <T> T loadAdapter(Class<T> adapterClass) throws ExceptionInInitializerError {
        final ClassDependency classDependency = adapterClass.getAnnotation(ClassDependency.class);

        Set<String> missingClasses = new HashSet<>();
        for (String depClass : (classDependency == null
                ? new String[0]
                : classDependency.value())) {
            try {
                Class.forName(depClass);
            } catch (ClassNotFoundException ignored) {
                missingClasses.add(depClass);
            }
        }
        if (!missingClasses.isEmpty()) throw new ExceptionInInitializerError(String.format(
                "Missing dependency classes:%s",
                missingClasses.stream()
                        .collect(Collectors.joining(
                                "\n\t-\t",
                                "",
                                "\n"
                        ))
        ));
        else System.err.printf(
                "Missing ClassDependency annotation on class %s\n",
                adapterClass.getName()
        );

        try {
            return ReflectionHelper.instance(adapterClass);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public final Junction<String, BAS> parser;
    public final DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType;
    public final DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType;

    protected SeriLib(
            Junction<String, BAS> parser,
            Class<OBJ> objClass,
            Class<ARR> arrClass
    ) {
        this(parser,
                new DataStructureType.Obj<>(objClass),
                new DataStructureType.Arr<>(arrClass)
        );
    }

    protected SeriLib(
            Junction<String, BAS> parser,
            DataStructureType.Obj<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> objectType,
            DataStructureType.Arr<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR> arrayType
    ) {
        this.parser = parser;
        this.objectType = objectType;
        this.arrayType = arrayType;

        ReflectionHelper.verifyClassDependencies(getClass());
    }

    @Override
    public String toString() {
        return String.format("SeriLib{lib=%s}", getClass().getName());
    }

    @Contract("null -> null")
    @Deprecated
    public <TAR extends BAS> @Nullable NodeDummy<SeriLib<BAS, OBJ, ARR>, BAS, OBJ, ARR, TAR> dummy(@Nullable TAR node) {
        throw new UnsupportedOperationException("Use SeriLib.createUniNode() methods instead!");
    }

    public abstract UniObjectNode createUniObjectNode(OBJ node);

    public abstract UniArrayNode createUniArrayNode(ARR node);

    public <TAR extends BAS> DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR> typeOf(TAR node) {
        if (objectType.typeClass()
                .isInstance(node))
            return (DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR>) objectType;
        if (arrayType.typeClass()
                .isInstance(node))
            return (DataStructureType<SeriLib<BAS, OBJ, ARR>, BAS, TAR>) arrayType;

        throw new IllegalArgumentException("Unknown type: " + node.getClass()
                .getName());
    }

    public final UniNode createUniNode(Object node) {
        if (objectType.typeClass().isInstance(node))
            return createUniObjectNode((OBJ) node);
        if (arrayType.typeClass().isInstance(node))
            return createUniArrayNode((ARR) node);

        throw new IllegalArgumentException(String.format("Unknown node type: %s", node.getClass().getName()));
    }

}
