package org.comroid.varbind;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.comroid.common.iter.Span;
import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.DataStructureType.Primitive;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.comroid.common.Polyfill.deadCast;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

@SuppressWarnings("unchecked")
public abstract class VariableCarrier<BAS, OBJ extends BAS, DEP>
        implements VarCarrier<BAS, OBJ, DEP> {
    private final SeriLib<BAS, OBJ, ? extends BAS>                             seriLib;
    private final Map<VarBind<?, ?, ?, ?, OBJ>, AtomicReference<Span<Object>>> vars = new ConcurrentHashMap<>();
    private final GroupBind<BAS, OBJ, ?>                                       rootBind;
    private final Set<VarBind<?, ?, ?, ?, OBJ>>                                initiallySet;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<DEP>                                                dependencyObject;

    protected <ARR extends BAS> VariableCarrier(
            SeriLib<BAS, OBJ, ARR> seriLib, @Nullable String data, @Nullable DEP dependencyObject
    ) {
        this(seriLib,
             data == null ? null : seriLib.objectType.cast(seriLib.parser.forward(data)),
             dependencyObject
        );
    }

    protected <ARR extends BAS> VariableCarrier(
            SeriLib<BAS, OBJ, ARR> seriLib, @Nullable OBJ node, @Nullable DEP dependencyObject
    ) {
        this.seriLib          = seriLib;
        this.rootBind         = findRootBind(getClass());
        this.initiallySet     = updateVars(seriLib.createUniObjectNode(node));
        this.dependencyObject = Optional.ofNullable(dependencyObject);
    }

    private <ARR extends BAS> GroupBind<BAS, OBJ, ARR> findRootBind(Class<? extends VarCarrier> inClass) {
        final VarBind.Location location = inClass.getAnnotation(VarBind.Location.class);

        if (location == null) throw new IllegalStateException(String.format(
                "Class %s extends VariableCarrier, but does not have a %s annotation.",
                inClass.getName(),
                VarBind.Location.class.getName()
        ));

        return (GroupBind<BAS, OBJ, ARR>) ReflectionHelper.collectStaticFields(GroupBind.class,
                                                                               location.value(),
                                                                               true,
                                                                               VarBind.Root.class
        )
                                                          .requireNonNull();
    }

    public <SERI extends SeriLib<BAS, OBJ, ARR>, ARR extends BAS, TAR extends BAS> Set<VarBind<?, ?, ?, ?, OBJ>> updateVars(
            @Nullable UniNode<BAS> data
    ) {
        if (data == null) return emptySet();

        if (data.getType() != Primitive.OBJECT)
            throw new IllegalArgumentException("Object required");

        final HashSet<VarBind<?, ?, DEP, ?, OBJ>> changed = new HashSet<>();
        for (VarBind<?, ?, ?, ?, OBJ> bind : this.rootBind.getChildren()) {
            if (data.containsKey(bind.getName())) {
                if (bind instanceof ArrayBind) {
                    final Span<Object> span = ((UniArrayNode) data).stream()
                                                                   .map(bind::extract)
                                                                   .flatMap(Span::stream)
                                                                   .map(Object.class::cast)
                                                                   .collect(Span.make()
                                                                                .fixedSize(true)
                                                                                .collector());

                    ref((VarBind<Object, Object, DEP, Object, OBJ>) bind).set(span);
                } else ref((VarBind<Object, Object, DEP, Object, OBJ>) bind).set((Span<Object>) bind.extract(
                        (OBJ) data.getBaseNode()));

                changed.add((VarBind<?, ?, DEP, ?, OBJ>) bind);
            }
        }

        return unmodifiableSet(changed);
    }

    private <C> AtomicReference<Span<C>> ref(VarBind<C, ?, ?, ?, OBJ> bind) {
        return deadCast(vars.computeIfAbsent(bind,
                                             key -> deadCast(new AtomicReference<>(Span.<C>make().initialSize(
                                                     1)
                                                                                                 .fixedSize(
                                                                                                         true)
                                                                                                 .span()))
        ));
    }

    @Override
    public final SeriLib<BAS, OBJ, ? extends BAS> getSerializationLibrary() {
        return seriLib;
    }

    @Override
    public final GroupBind<BAS, OBJ, ?> getBindings() {
        return rootBind;
    }

    @Override
    public final Set<VarBind<?, ?, ?, ?, OBJ>> updateFrom(OBJ node) {
        return updateVars(seriLib.createUniObjectNode(node));
    }

    @Override
    public final Set<VarBind<?, ?, ?, ?, OBJ>> initiallySet() {
        return initiallySet;
    }

    @Override
    public final <T, A, R> @NotNull Optional<R> wrapVar(VarBind<T, A, ?, R, OBJ> bind) {
        return Optional.ofNullable(getVar(bind));
    }

    @Override
    public final <T, A, R> @Nullable R getVar(VarBind<T, A, ?, R, OBJ> bind) {
        final Span<A> span = ref(bind).get()
                                      .stream()
                                      .map(it -> it == null
                                              ? null
                                              : bind.remap(it,
                                                           deadCast(dependencyObject.orElse(null))
                                              ))
                                      .filter(Objects::nonNull)
                                      .collect(Span.<A>make().fixedSize(true)
                                                             .collector());

        if (span.isEmpty()) {
            if (bind instanceof ArrayBind) return bind.finish(Span.zeroSize());
            else return null;
        } else return bind.finish(span);
    }
}
