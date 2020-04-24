package org.comroid.listnr;

import java.util.function.Predicate;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.util.BitmaskUtil;
import org.comroid.spellbind.Spellbind;
import org.comroid.spellbind.annotation.Partial;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import static org.comroid.common.util.BitmaskUtil.combine;

@Partial
public interface EventType<P extends Event<? super P>, I, O> extends ParamFactory<O, P> {
    @Internal
    int getMask();

    default boolean isCombined() {
        return this instanceof Combined;
    }

    @Override
    P create(@Nullable O parameter);

    boolean isEvent(@Nullable O data);

    Class<P> payloadType();

    final class Support {
        static final class Basic<P extends Event<P>, I, O> implements EventType<P, I, O> {
            Basic(
                    EventHub<I, O> hub, Class<P> payloadType, Predicate<O> eventTester, ParamFactory<O, P> payloadFactory
            ) {
                this.hub            = hub;
                this.payloadType    = payloadType;
                this.eventTester    = eventTester;
                this.payloadFactory = payloadFactory;

                hub.registerEventType(this);
            }

            @Override
            public final int getMask() {
                return flag;
            }

            @Override
            public final P create(@Nullable O parameter) {
                return payloadFactory.create(parameter);
            }

            @Override
            public final boolean isEvent(@Nullable O data) {
                return eventTester.test(data);
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
            protected final EventHub<I, O>     hub;
            private final   Class<P>           payloadType;
            private final   int                flag = BitmaskUtil.nextFlag();
            private final   Predicate<O>       eventTester;
            private final   ParamFactory<O, P> payloadFactory;
        }

        private static final class Combination<P extends Event<P>, I, O> implements Combined<P, I, O> {
            private Combination(EventType<? super P, I, O>[] subtypes, Predicate<O> eventTester, Class<P> payloadType) {
                this.eventTester = eventTester;
                this.payloadType = payloadType;
                this.subtypes    = subtypes;
                this.mask        = computeMask();
            }

            private int computeMask() {
                int yield = BitmaskUtil.EMPTY;
                for (EventType<? super P, I, O> type : subtypes) {
                    yield = combine(yield, type.getMask());
                }
                return yield;
            }

            @Override
            public int getMask() {
                return mask;
            }

            @Override
            public P create(@Nullable O parameter) {
                Spellbind.Builder<P> payloadCombinator = Spellbind.builder(payloadType);

                payloadCombinator.coreObject(new Event.Support.Abstract<P>() {});
                for (EventType<? super P, I, O> subtype : subtypes) {
                    payloadCombinator.subImplement(subtype);
                }

                return payloadCombinator.build();
            }

            @Override
            public boolean isEvent(@Nullable O data) {
                return eventTester.test(data);
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
            private final Predicate<O>                 eventTester;
            private final Class<P>                     payloadType;
            private final EventType<? super P, I, O>[] subtypes;
            private final int                          mask;
        }
    }

    interface Combined<P extends Event<P>, I, O> extends EventType<P, I, O> {
        @SafeVarargs
        static <P extends Event<P>, I, O> Combined<P, I, O> of(
                Class<P> payloadInterface, Predicate<O> eventTester, EventType<? super P, I, O>... subtypes
        ) {
            return new EventType.Support.Combination<>(subtypes, eventTester, payloadInterface);
        }
    }
}
