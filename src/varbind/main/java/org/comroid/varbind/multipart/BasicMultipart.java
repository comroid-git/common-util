package org.comroid.varbind.multipart;

import org.comroid.api.Invocable;
import org.comroid.api.Polyfill;
import org.comroid.api.UUIDContainer;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.comroid.varbind.bind.GroupBind;

public final class BasicMultipart {
    private static final TypeFragmentProvider<PartialBind.Base> BASE_PROVIDER = new FragmentProviders.Base();

    public static TypeFragmentProvider<PartialBind.Base> baseProvider() {
        return BASE_PROVIDER;
    }

    public static <D> TypeFragmentProvider<PartialBind.Grouped<D>> groupedProvider() {
        return new FragmentProviders.Grouped<>();
    }

    public static final class Base extends UUIDContainer implements PartialBind.Base {
        private static final Invocable<? super Base> constructor = Invocable.ofConstructor(Base.class);
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
        private static final class Base implements TypeFragmentProvider<PartialBind.Base> {
            @Override
            public Class<PartialBind.Base> getInterface() {
                return PartialBind.Base.class;
            }

            @Override
            public Invocable.TypeMap<? extends PartialBind.Base> getInstanceSupplier() {
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
