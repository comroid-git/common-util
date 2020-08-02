package org.comroid.varbind.bind;

import org.comroid.api.Builder;
import org.comroid.api.Invocable;
import org.comroid.spellbind.SpellCore;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.varbind.container.DataContainer;
import org.comroid.varbind.multipart.*;
import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.comroid.api.Polyfill.uncheckedCast;

public final class BindBuilder<SELF extends DataContainer<? super SELF>, EXTR, REMAP, FINAL> implements Builder<VarBind<SELF, EXTR, REMAP, FINAL>> {
    private final GroupBind<SELF> groupBind;
    private final String fieldName;

    private final TypeFragmentProvider<PartialBind.Base<SELF, EXTR, REMAP, FINAL>> baseProvider = BasicMultipart.baseProvider();
    private final TypeFragmentProvider<PartialBind.Grouped<SELF>> groupedProvider = BasicMultipart.groupedProvider();
    private TypeFragmentProvider<PartialBind.Extractor<EXTR>> extractorProvider = null;
    private TypeFragmentProvider<PartialBind.Remapper<SELF, EXTR, REMAP>> remapperProvider = null;
    private TypeFragmentProvider<PartialBind.Finisher<REMAP, FINAL>> finisherProvider = null;
    private boolean required = false;
    private ValueType<? extends EXTR> valueType = null;
    private Function<EXTR, REMAP> remapper = null;
    private BiFunction<EXTR, Object, REMAP> resolver = null;
    private Supplier<? extends Collection<REMAP>> collectionProvider = null;
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public GroupBind<SELF> getGroupBind() {
        return groupBind;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isRequired() {
        return required;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public BindBuilder<SELF, EXTR, REMAP, FINAL> setRequired(boolean required) {
        this.required = required;

        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public BindBuilder<SELF, EXTR, REMAP, FINAL> setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;

        return this;
    }

    public BindBuilder(GroupBind<SELF> groupBind, String fieldName) {
        this.groupBind = groupBind;
        this.fieldName = fieldName;
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<SELF, EXTR, REMAP, FINAL> setRequired() {
        return setRequired(true);
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<SELF, EXTR, REMAP, FINAL> setOptional() {
        return setRequired(false);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <E extends Serializable> BindBuilder<SELF, E, REMAP, FINAL> extractAs(ValueType<E> valueType) {
        this.valueType = uncheckedCast(valueType);
        this.extractorProvider = uncheckedCast(ExtractingBind.valueTypeExtractingProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<SELF, UniObjectNode, REMAP, FINAL> extractAsObject() {
        this.valueType = null;
        this.extractorProvider = uncheckedCast(ExtractingBind.objectExtractingProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<SELF, UniArrayNode, REMAP, FINAL> extractAsArray() {
        this.valueType = null;
        this.extractorProvider = uncheckedCast(ExtractingBind.arrayExtractingProvider());

        return uncheckedCast(this);
    }

    public BindBuilder<SELF, EXTR, EXTR, FINAL> asIdentities() {
        this.remapper = null;
        this.resolver = null;
        this.remapperProvider = uncheckedCast((Object) StagedBind.oneStageProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <R> BindBuilder<SELF, EXTR, R, FINAL> andRemap(Function<EXTR, R> remapper) {
        this.remapper = uncheckedCast(remapper);
        this.resolver = null;
        this.remapperProvider = uncheckedCast((Object) StagedBind.twoStageProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <R> BindBuilder<SELF, EXTR, R, FINAL> andResolve(BiFunction<EXTR, Object, R> resolver) {
        this.remapper = null;
        this.resolver = uncheckedCast(resolver);
        this.remapperProvider = uncheckedCast((Object) StagedBind.dependentTwoStageProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <R extends DataContainer<? extends Object>> BindBuilder<SELF, UniObjectNode, R, FINAL> andConstruct(GroupBind<SELF> targetBind) {
        return uncheckedCast(
                andResolve(targetBind.getConstructor()
                        .map(Invocable::<EXTR, Object>biFunction)
                        .orElseThrow(() -> new NoSuchElementException("No Constructor in " + targetBind))));
    }

    @Contract(value = "_,_,_ -> this", mutates = "this")
    public <R extends DataContainer<? extends Object>, ID> BindBuilder<SELF, UniObjectNode, R, FINAL> andProvide(
            VarBind<SELF, ?, ?, ID> idBind,
            BiFunction<ID, Object, R> resolver,
            GroupBind<SELF> targetBind
    ) {
        return uncheckedCast(
                andResolve((obj, dpnd) -> {
                    if (!(obj instanceof UniObjectNode))
                        throw new IllegalStateException();
                    final ID id = idBind.getFrom((UniObjectNode) obj);
                    final R firstResult = resolver.apply(id, dpnd);

                    if (firstResult == null)
                        return targetBind.getConstructor()
                                .map(constr -> constr.autoInvoke(obj, dpnd, id))
                                .orElseThrow(() -> new NoSuchElementException("Could not instantiate " + targetBind));
                    return firstResult;
                }));
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<SELF, EXTR, REMAP, REMAP> onceEach() {
        this.collectionProvider = null;
        this.finisherProvider = uncheckedCast(FinishedBind.singleResultProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <C extends Collection<REMAP>> BindBuilder<SELF, EXTR, REMAP, C> intoCollection(Supplier<C> collectionProvider) {
        this.collectionProvider = collectionProvider;
        this.finisherProvider = uncheckedCast(FinishedBind.collectingProvider());

        return uncheckedCast(this);
    }

    @Override
    public VarBind<SELF, EXTR, REMAP, FINAL> build() {
        final PartialBind.Base<SELF, EXTR, REMAP, FINAL> core = baseProvider.getInstanceSupplier().autoInvoke(fieldName, required);
        final SpellCore.Builder<VarBind<SELF, EXTR, REMAP, FINAL>> builder = SpellCore
                .<VarBind<SELF, EXTR, REMAP, FINAL>>builder(uncheckedCast(VarBind.class), core)
                .addFragment(groupedProvider)
                .addFragment(Objects.requireNonNull(extractorProvider, "No extractor definition provided"))
                .addFragment(Objects.requireNonNull(remapperProvider, "No remapper defintion provided"))
                .addFragment(Objects.requireNonNull(finisherProvider, "No finisher definition provided"))
                .setClassLoader(classLoader);

        final VarBind<SELF, EXTR, REMAP, FINAL> bind = builder.build(Stream
                .of(groupBind, fieldName, required, valueType, remapper, resolver, collectionProvider)
                .filter(Objects::nonNull)
                .toArray()
        );

        groupBind.addChild(bind);
        return bind;
    }
}
