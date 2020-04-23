package org.comroid.listnr;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.func.Provider;
import org.comroid.common.spellbind.Spellbind;
import org.comroid.common.spellbind.annotation.Partial;
import org.comroid.common.util.BitmaskUtil;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import static org.comroid.common.util.BitmaskUtil.combine;

@Partial
public interface EventType<P extends Event<P>, TF> extends ParamFactory<TF, P> {
    final class Support {
        static class Basic<P extends Event<P>, TF> implements EventType<P, TF> {
            protected final EventHub<TF>        hub;
            private final   int                 flag = BitmaskUtil.nextFlag();
            private final   ParamFactory<TF, P> payloadFactory;

            Basic(EventHub<TF> hub, ParamFactory<TF, P> payloadFactory) {
                this.hub            = hub;
                this.payloadFactory = payloadFactory;

                hub.registerEventType(this);
            }

            @Override
            public int getMask() {
                return flag;
            }

            @Override
            public P create(@Nullable TF parameter) {
                return payloadFactory.create(parameter);
            }

            @Override
            public int counter() {
                return payloadFactory.counter();
            }

            @Override
            public int peekCounter() {
                return payloadFactory.peekCounter();
            }
        }

        private static final class Combination<P extends Event<P>, TF> implements Combined<P, TF> {
            private final EventType<? super P, TF>[]     subtypes;
            private final Provider<Spellbind.Builder<P>> payloadCombinatorProvider;
            private final int                            mask;

            private Combination(
                    EventType<? super P, TF>[] subtypes,
                    Now<Spellbind.Builder<P>> payloadCombinatorProvider
            ) {
                this.subtypes                  = subtypes;
                this.payloadCombinatorProvider = payloadCombinatorProvider;
                this.mask                      = computeMask();
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
                Spellbind.Builder<P> payloadCombinator = payloadCombinatorProvider.now();

                payloadCombinator.coreObject(new Event.Support.Abstract<P>() {});
                for (EventType<? super P, TF> subtype : subtypes) {
                    payloadCombinator.subImplement(subtype);
                }

                return payloadCombinator.build();
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

    interface Combined<P extends Event<P>, TF> extends EventType<P, TF> {
        @SafeVarargs
        static <P extends Event<P>, TF> Combined<P, TF> of(
                Class<P> payloadInterface, EventType<? super P, TF>... subtypes
        ) {
            return new EventType.Support.Combination<>(
                    subtypes,
                    () -> Spellbind.builder(payloadInterface)
            );
        }
    }
}
