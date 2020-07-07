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

public final class BindBuilder<EXTR, DPND, REMAP, FINAL> implements Builder<VarBind<EXTR, DPND, REMAP, FINAL>> {
    private final GroupBind<?, DPND> groupBind;
    private final String fieldName;

    private final TypeFragmentProvider<PartialBind.Base<EXTR, DPND, REMAP, FINAL>> baseProvider = BasicMultipart.baseProvider();
    private final TypeFragmentProvider<PartialBind.Grouped<DPND>> groupedProvider = BasicMultipart.groupedProvider();
    private TypeFragmentProvider<PartialBind.Extractor<EXTR>> extractorProvider = null;
    private TypeFragmentProvider<PartialBind.Remapper<EXTR, DPND, REMAP>> remapperProvider = null;
    private TypeFragmentProvider<PartialBind.Finisher<REMAP, FINAL>> finisherProvider = null;
    private boolean required = false;
    private ValueType<? extends EXTR> valueType = null;
    private Function<EXTR, REMAP> remapper = null;
    private BiFunction<EXTR, DPND, REMAP> resolver = null;
    private Supplier<? extends Collection<REMAP>> collectionProvider = null;
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public GroupBind<?, DPND> getGroupBind() {
        return groupBind;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isRequired() {
        return required;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public BindBuilder<EXTR, DPND, REMAP, FINAL> setRequired(boolean required) {
        this.required = required;

        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public BindBuilder<EXTR, DPND, REMAP, FINAL> setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;

        return this;
    }

    public BindBuilder(GroupBind<?, DPND> groupBind, String fieldName) {
        this.groupBind = groupBind;
        this.fieldName = fieldName;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <E extends Serializable> BindBuilder<E, DPND, REMAP, FINAL> extractAs(ValueType<E> valueType) {
        this.valueType = uncheckedCast(valueType);
        this.extractorProvider = uncheckedCast(ExtractingBind.valueTypeExtractingProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<UniObjectNode, DPND, REMAP, FINAL> extractAsObject() {
        this.valueType = null;
        this.extractorProvider = uncheckedCast(ExtractingBind.objectExtractingProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<UniArrayNode, DPND, REMAP, FINAL> extractAsArray() {
        this.valueType = null;
        this.extractorProvider = uncheckedCast(ExtractingBind.arrayExtractingProvider());

        return uncheckedCast(this);
    }

    public BindBuilder<EXTR, DPND, EXTR, FINAL> asIdentities() {
        this.remapper = null;
        this.resolver = null;
        this.remapperProvider = uncheckedCast((Object) StagedBind.oneStageProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <R> BindBuilder<EXTR, DPND, R, FINAL> andRemap(Function<EXTR, R> remapper) {
        this.remapper = uncheckedCast(remapper);
        this.resolver = null;
        this.remapperProvider = uncheckedCast((Object) StagedBind.twoStageProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <R> BindBuilder<EXTR, DPND, R, FINAL> andResolve(BiFunction<EXTR, DPND, R> resolver) {
        this.remapper = null;
        this.resolver = uncheckedCast(resolver);
        this.remapperProvider = uncheckedCast((Object) StagedBind.dependentTwoStageProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <R extends DataContainer<? extends DPND>> BindBuilder<UniObjectNode, DPND, R, FINAL> andConstruct(GroupBind<R, DPND> targetBind) {
        return uncheckedCast(
                andResolve(targetBind.getConstructor()
                        .map(Invocable::<EXTR, DPND>biFunction)
                        .orElseThrow(() -> new NoSuchElementException("No Constructor in " + targetBind))));
    }

    @Contract(value = "-> this", mutates = "this")
    public BindBuilder<EXTR, DPND, REMAP, REMAP> onceEach() {
        this.collectionProvider = null;
        this.finisherProvider = uncheckedCast(FinishedBind.singleResultProvider());

        return uncheckedCast(this);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <C extends Collection<REMAP>> BindBuilder<EXTR, DPND, REMAP, C> intoCollection(Supplier<C> collectionProvider) {
        this.collectionProvider = collectionProvider;
        this.finisherProvider = uncheckedCast(FinishedBind.collectingProvider());

        return uncheckedCast(this);
    }

    @Override
    public VarBind<EXTR, DPND, REMAP, FINAL> build() {
        final PartialBind.Base<EXTR, DPND, REMAP, FINAL> core = baseProvider.getInstanceSupplier().autoInvoke(fieldName, required);
        final SpellCore.Builder<VarBind<EXTR, DPND, REMAP, FINAL>> builder = SpellCore
                .<VarBind<EXTR, DPND, REMAP, FINAL>>builder(uncheckedCast(VarBind.class), core)
                .addFragment(groupedProvider)
                .addFragment(Objects.requireNonNull(extractorProvider, "No extractor definition provided"))
                .addFragment(Objects.requireNonNull(remapperProvider, "No remapper defintion provided"))
                .addFragment(Objects.requireNonNull(finisherProvider, "No finisher definition provided"))
                .setClassLoader(classLoader);

        final VarBind<EXTR, DPND, REMAP, FINAL> bind = builder.build(Stream
                .of(groupBind, fieldName, required, valueType, remapper, resolver, collectionProvider)
                .filter(Objects::nonNull)
                .toArray()
        );

        groupBind.addChild(bind);
        return bind;
    }
}
