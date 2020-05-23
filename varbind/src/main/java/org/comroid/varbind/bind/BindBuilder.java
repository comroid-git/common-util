package org.comroid.varbind.bind;

import org.comroid.common.func.Builder;
import org.comroid.spellbind.Spellbind;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.comroid.uniform.ValueType;
import org.comroid.varbind.multipart.*;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.comroid.common.Polyfill.uncheckedCast;

public final class BindBuilder<EXTR, DPND, REMAP, FINAL> implements Builder<VarBind<EXTR, DPND, REMAP, FINAL>> {
    private final GroupBind<?, DPND> groupBind;
    private final String fieldName;

    private final TypeFragmentProvider<PartialBind.Base> baseProvider = BasicMultipart.baseProvider();
    private final TypeFragmentProvider<PartialBind.Grouped<DPND>> groupedProvider = BasicMultipart.groupedProvider();
    private TypeFragmentProvider<PartialBind.Extractor<EXTR>> extractorProvider = uncheckedCast(ExtractingBind.valueTypeExtractingProvider());
    private TypeFragmentProvider<PartialBind.Remapper<EXTR, DPND, REMAP>> remapperProvider = uncheckedCast(StagedBind.oneStageProvider());
    private TypeFragmentProvider<PartialBind.Finisher<REMAP, FINAL>> finisherProvider = uncheckedCast(FinishedBind.singleResultProvider());
    private boolean required = false;
    private ValueType<? extends EXTR> valueType = null;
    private Function<EXTR, REMAP> remapper = null;
    private BiFunction<EXTR, DPND, REMAP> resolver = null;
    private Supplier<? extends Collection<REMAP>> collectionProvider;
    private ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public GroupBind<?, DPND> getGroupBind() {
        return groupBind;
    }

    public String getFieldName() {
        return fieldName;
    }

    public TypeFragmentProvider<PartialBind.Base> getBaseProvider() {
        return baseProvider;
    }

    public TypeFragmentProvider<PartialBind.Grouped<DPND>> getGroupedProvider() {
        return groupedProvider;
    }

    public TypeFragmentProvider<PartialBind.Extractor<EXTR>> getExtractorProvider() {
        return extractorProvider;
    }

    public TypeFragmentProvider<PartialBind.Remapper<EXTR, DPND, REMAP>> getRemapperProvider() {
        return remapperProvider;
    }

    public TypeFragmentProvider<PartialBind.Finisher<REMAP, FINAL>> getFinisherProvider() {
        return finisherProvider;
    }

    public boolean isRequired() {
        return required;
    }

    public BindBuilder<EXTR, DPND, REMAP, FINAL> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public ValueType<? extends EXTR> getValueType() {
        return valueType;
    }

    public BindBuilder<EXTR, DPND, REMAP, FINAL> setValueType(ValueType<? extends EXTR> valueType) {
        if (valueType != null)
            this.extractorProvider = uncheckedCast(ExtractingBind.valueTypeExtractingProvider());
        else this.extractorProvider = uncheckedCast(ExtractingBind.objectExtractingProvider());

        this.valueType = valueType;
        return this;
    }

    public Function<EXTR, REMAP> getRemapper() {
        return remapper;
    }

    public BindBuilder<EXTR, DPND, REMAP, FINAL> setRemapper(Function<EXTR, REMAP> remapper) {
        if (remapper != null)
            this.remapperProvider = uncheckedCast(StagedBind.twoStageProvider());
        else this.remapperProvider = uncheckedCast(StagedBind.oneStageProvider());

        this.remapper = remapper;
        return this;
    }

    public BiFunction<EXTR, DPND, REMAP> getResolver() {
        return resolver;
    }

    public BindBuilder<EXTR, DPND, REMAP, FINAL> setResolver(BiFunction<EXTR, DPND, REMAP> resolver) {
        if (resolver != null)
            this.remapperProvider = uncheckedCast(StagedBind.dependentTwoStageProvider());
        else this.remapperProvider = uncheckedCast(StagedBind.oneStageProvider());

        this.resolver = resolver;
        return this;
    }

    public Supplier<? extends Collection<REMAP>> getCollectionProvider() {
        return collectionProvider;
    }

    public BindBuilder<EXTR, DPND, REMAP, FINAL> setCollectionProvider(Supplier<? extends Collection<REMAP>> collectionProvider) {
        if (collectionProvider != null)
            this.finisherProvider = uncheckedCast(FinishedBind.collectingProvider());
        else this.finisherProvider = uncheckedCast(FinishedBind.singleResultProvider());

        this.collectionProvider = collectionProvider;
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public BindBuilder<EXTR, DPND, REMAP, FINAL> setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public BindBuilder(GroupBind<?, DPND> groupBind, String fieldName) {
        this.groupBind = groupBind;
        this.fieldName = fieldName;
    }

    @Override
    public VarBind<EXTR, DPND, REMAP, FINAL> build() {
        final Spellbind.Builder<VarBind<EXTR, DPND, REMAP, FINAL>> builder
                = uncheckedCast(Spellbind.builder(VarBind.class).classloader(classLoader));

        final PartialBind.Base core = baseProvider.getInstanceSupplier().autoInvoke(fieldName, required);

        builder.coreObject(core);
        groupedProvider.accept(builder, groupBind);
        extractorProvider.accept(builder, valueType);
        remapperProvider.accept(builder, remapper, resolver);
        finisherProvider.accept(builder, collectionProvider);

        return builder.build();
    }
}
