package old;

import java.util.function.Predicate;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.util.Bitmask;
import org.comroid.spellbind.Spellbind;
import org.comroid.spellbind.annotation.Partial;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import static org.comroid.common.util.Bitmask.combine;

@Partial
public interface EventType<I, O, P extends Event<? super P>> extends ParamFactory<O, P> {
    @Internal
    int getMask();

    default boolean isCombined() {
        return this instanceof Combined;
    }

    @Override
    P create(@Nullable O parameter);

    boolean isEvent(@Nullable O data);

    Class<P> payloadType();

    interface Combined<P extends Event<? super P>, I, O> extends EventType<I, O, P> {
        @SafeVarargs
        static <P extends Event<? super P>, I, O> Combined<P, I, O> of(
                Class<P> payloadInterface, Predicate<O> eventTester, EventType<I, O, ? super P>... subtypes
        ) {
            return new EventType.Support.Combination<>(subtypes, eventTester, payloadInterface);
        }
    }

    final class Support {
        public static class Basic<I, O, P extends Event<? super P>> implements EventType<I, O, P> {
            protected final EventHub<I, O, ? extends EventType<? super I, ? super O, ? super P>, P> hub;
            private final   Class<P>                                                                payloadType;
            private final   int                                                                     flag = Bitmask.nextFlag();
            private final   Predicate<O>                                                            eventTester;
            private final   ParamFactory<O, P>                                                      payloadFactory;

            protected Basic(
                    EventHub<I, O, ?, P> hub, Class<P> payloadType, Predicate<O> eventTester, ParamFactory<O, P> payloadFactory
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
        }

        public static class Combination<P extends Event<? super P>, I, O> implements Combined<P, I, O> {
            private final Predicate<O>                 eventTester;
            private final Class<P>                     payloadType;
            private final EventType<I, O, ? super P>[] subtypes;
            private final int                          mask;

            protected Combination(EventType<I, O, ? super P>[] subtypes, Predicate<O> eventTester, Class<P> payloadType) {
                this.eventTester = eventTester;
                this.payloadType = payloadType;
                this.subtypes    = subtypes;
                this.mask        = computeMask();
            }

            private int computeMask() {
                int yield = Bitmask.EMPTY;
                for (EventType<I, O, ? super P> type : subtypes) {
                    yield = combine(yield, type.getMask());
                }
                return yield;
            }

            @Override
            public final int getMask() {
                return mask;
            }

            @Override
            public final P create(@Nullable O parameter) {
                Spellbind.Builder<P> payloadCombinator = Spellbind.builder(payloadType);

                payloadCombinator.coreObject(new Event.Support.Abstract<P>() {});
                for (EventType<I, O, ? super P> subtype : subtypes) {
                    payloadCombinator.subImplement(subtype);
                }

                return payloadCombinator.build();
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
                return c++;
            }

            @Override
            public final int peekCounter() {
                return c;
            }

            private int c = 0;
        }
    }
}
