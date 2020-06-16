package org.comroid.varbind.multipart;

import org.comroid.api.Invocable;
import org.comroid.api.Polyfill;
import org.comroid.api.UUIDContainer;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.comroid.varbind.bind.GroupBind;

public final class BasicMultipart {
    public static <EXTR, DPND, REMAP, FINAL> TypeFragmentProvider<PartialBind.Base<EXTR, DPND, REMAP, FINAL>> baseProvider() {
        return new FragmentProviders.Base<>();
    }

    public static <D> TypeFragmentProvider<PartialBind.Grouped<D>> groupedProvider() {
        return new FragmentProviders.Grouped<>();
    }

    public static final class Base<EXTR, DPND, REMAP, FINAL> extends UUIDContainer implements PartialBind.Base<EXTR, DPND, REMAP, FINAL> {
        private static final Invocable<? super Base<?, ?, ?, ?>> constructor = Invocable.ofConstructor(Base.class);
        private final String fieldName;
        private final boolean required;

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        public Base(String fieldName, boolean required) {
            this.fieldName = fieldName;
            this.required = required;
        }
    }

    public static final class Grouped<D> extends UUIDContainer implements PartialBind.Grouped<D> {
        private static final Invocable<? super Grouped<?>> constructor = Invocable.ofConstructor(Grouped.class);
        private final GroupBind<?, D> group;

        @Override
        public GroupBind<?, D> getGroup() {
            return group;
        }

        public Grouped(GroupBind<?, D> group) {
            this.group = group;
        }
    }

    private static final class FragmentProviders {
        private static final class Base<EXTR, DPND, REMAP, FINAL> implements TypeFragmentProvider<PartialBind.Base<EXTR, DPND, REMAP, FINAL>> {
            @Override
            public Class<PartialBind.Base<EXTR, DPND, REMAP, FINAL>> getInterface() {
                return Polyfill.uncheckedCast(PartialBind.Base.class);
            }

            @Override
            public Invocable.TypeMap<? extends PartialBind.Base<EXTR, DPND, REMAP, FINAL>> getInstanceSupplier() {
                return Polyfill.uncheckedCast(BasicMultipart.Base.constructor.typeMapped());
            }
        }

        private static final class Grouped<D> implements TypeFragmentProvider<PartialBind.Grouped<D>> {
            @Override
            public Class<PartialBind.Grouped<D>> getInterface() {
                return Polyfill.uncheckedCast(PartialBind.Grouped.class);
            }

            @Override
            public Invocable.TypeMap<? extends PartialBind.Grouped<D>> getInstanceSupplier() {
                return Polyfill.uncheckedCast(BasicMultipart.Grouped.constructor.typeMapped());
            }
        }
    }
}
