package org.comroid.listnr.model;

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.info.Dependent;
import org.comroid.common.ref.Reference;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.common.util.ArrayUtil;
import org.comroid.spellbind.factory.InstanceFactory;
import org.comroid.spellbind.model.TypeFragmentProvider;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface EventType<IN, D,
        ET extends EventType<IN, D, ET, EP>,
        EP extends EventPayload<D, ET, EP>>
        extends Predicate<IN>, Dependent<D>, TypeFragmentProvider<EP>, SelfDeclared<ET> {
    Collection<? super ET> getParents();

    Collection<? extends ET> getChildren();

    Invocable.TypeMap<? extends EP> getPayloadConstructor();

    @Override
    boolean test(IN in);

    @Internal
    <NT extends ET> void addChild(NT child);

    default EP makePayload(Object... input) {
        return makePayload(getPayloadConstructor(), input);
    }

    default <P extends EP> P makePayload(Invocable<? extends P> constructor, Object... input) {
        return constructor.autoInvoke(ArrayUtil.insert(input, input.length, getDependent()));
    }

    abstract class Basic<IN, D,
            ET extends EventType<IN, D, ET, EP>,
            EP extends EventPayload<D, ET, EP>>
            implements EventType<IN, D, ET, EP>, Reference<EP> {
        private final Collection<? super ET> parents;
        private final List<? extends ET> children = new ArrayList<>();
        private final InstanceFactory<EP, D> payloadFactory;
        private final Class<EP> payloadType;
        private final D dependent;

        @Override
        public final Collection<? super ET> getParents() {
            return parents;
        }

        @Override
        public final Collection<? extends ET> getChildren() {
            return children;
        }

        @Override
        public final Invocable.TypeMap<? extends EP> getPayloadConstructor() {
            return payloadFactory;
        }

        @Override
        public final D getDependent() {
            return dependent;
        }

        @Override
        public final Class<EP> getInterface() {
            return payloadType;
        }

        public Basic(Collection<? super ET> parents, Class<EP> payloadType, D dependent) {
            //noinspection unchecked
            parents.forEach(p -> ((ET) p)
                    .addChild(Polyfill.uncheckedCast(this)));

            this.parents = parents;
            this.payloadType = payloadType;
            this.dependent = dependent;

            //noinspection SuspiciousToArrayCall
            this.payloadFactory = new InstanceFactory<>(
                    payloadType,
                    this,
                    dependent,
                    parents.toArray(new EventType[0])
            );
        }

        @Override
        public @Nullable EP get() {
            return getInstanceSupplier().autoInvoke(getDependent());
        }

        @Override
        public final <NT extends ET> void addChild(NT child) {
            children.add(Polyfill.uncheckedCast(child));
        }

        @Override
        public boolean test(IN in) {
            return Stream.concat(Stream.of(this), getChildren().stream())
                    .allMatch(it -> it.test(Polyfill.uncheckedCast(in)));
        }
    }
}
