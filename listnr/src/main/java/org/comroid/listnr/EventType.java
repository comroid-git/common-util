package org.comroid.listnr;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.spellbind.Spellbind;
import org.comroid.common.spellbind.annotation.Partial;
import org.comroid.common.util.BitmaskUtil;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import static org.comroid.common.util.BitmaskUtil.combine;

@Partial
public interface EventType<P extends Event<? super P>, TF> extends ParamFactory<TF, P> {
    final class Support {
        static final class Basic<P extends Event<P>, TF> implements EventType<P, TF> {
            protected final EventHub<TF>        hub;
            private final   Class<P>            payloadType;
            private final   int                 flag = BitmaskUtil.nextFlag();
            private final   ParamFactory<TF, P> payloadFactory;

            Basic(
                    EventHub<TF> hub, Class<P> payloadType, ParamFactory<TF, P> payloadFactory
            ) {
                this.hub            = hub;
                this.payloadType    = payloadType;
                this.payloadFactory = payloadFactory;

                hub.registerEventType(this);
            }

            @Override
            public final int getMask() {
                return flag;
            }

            @Override
            public final P create(@Nullable TF parameter) {
                return payloadFactory.create(parameter);
            }

            @Override
            public final Class<P> payloadType() {
                return payloadType;
            }

            @Override
            public final int counter() {
                return payloadFactory.counter();
            }

            @Override
            public final int peekCounter() {
                return payloadFactory.peekCounter();
            }
        }

        private static final class Combination<P extends Event<P>, TF> implements Combined<P, TF> {
            private final Class<P>                   payloadType;
            private final EventType<? super P, TF>[] subtypes;
            private final int                        mask;

            private Combination(EventType<? super P, TF>[] subtypes, Class<P> payloadType) {
                this.payloadType = payloadType;
                this.subtypes    = subtypes;
                this.mask        = computeMask();
            }

            private int computeMask() {
                int yield = BitmaskUtil.EMPTY;
                for (EventType<? super P, TF> type : subtypes) {
                    yield = combine(yield, type.getMask());
                }
                return yield;
            }

            @Override
            public int getMask() {
                return mask;
            }

            @Override
            public P create(@Nullable TF parameter) {
                Spellbind.Builder<P> payloadCombinator = Spellbind.builder(payloadType);

                payloadCombinator.coreObject(new Event.Support.Abstract<P>() {});
                for (EventType<? super P, TF> subtype : subtypes) {
                    payloadCombinator.subImplement(subtype);
                }

                return payloadCombinator.build();
            }

            @Override
            public Class<P> payloadType() {
                return payloadType;
            }

            @Override
            public int counter() {
                return 0;
            }

            @Override
            public int peekCounter() {
                return 0;
            }
        }
    }

    @Internal
    int getMask();

    default boolean isCombined() {
        return this instanceof Combined;
    }

    @Override
    P create(@Nullable TF parameter);

    Class<P> payloadType();

    interface Combined<P extends Event<P>, TF> extends EventType<P, TF> {
        @SafeVarargs
        static <P extends Event<P>, TF> Combined<P, TF> of(
                Class<P> payloadInterface, EventType<? super P, TF>... subtypes
        ) {
            return new EventType.Support.Combination<>(subtypes, payloadInterface);
        }
    }
}
