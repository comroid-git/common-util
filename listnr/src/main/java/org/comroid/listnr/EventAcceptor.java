package org.comroid.listnr;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.comroid.common.annotation.vanity.inheritance.ShouldExtend;
import org.comroid.common.func.Invocable;
import org.comroid.common.util.Bitmask;

import org.jetbrains.annotations.ApiStatus.Internal;

import static org.comroid.common.Polyfill.deadCast;
import static org.comroid.common.util.Bitmask.combine;

@ShouldExtend(EventAcceptor.Support.Abstract.class)
public interface EventAcceptor<E extends EventType<? super P, ?, ?>, P extends Event<? super P>> {
    boolean canAccept(EventType<P, ?, ?> eventType);

    @Internal
    int getAcceptedTypesAsMask();

    @Internal
    <T extends P> void acceptEvent(T eventPayload);

    static <E extends EventType<P, ?, ?>, P extends Event<P>> EventAcceptor<E, P> ofMethod(Method method) {
        if (!method.isAnnotationPresent(Listnr.class)) {
            throw new IllegalArgumentException("EventHandler annotation not present");
        }
        final Listnr handler = method.getAnnotation(Listnr.class);

        return new Support.OfInvocable<>(Invocable.ofMethodCall(method));
    }

    static <E extends EventType<? super P, ?, ?>, P extends Event<? super P>> EventAcceptor<E, P> ofConsumer(
            Class<P> payloadType, Consumer<P> consumer
    ) {
        return new Support.OfInvocable<>(Invocable.ofConsumer(payloadType, consumer));
    }

    final class Support {
        public static abstract class Abstract<E extends EventType<? super P, ?, ?>, P extends Event<? super P>> implements EventAcceptor<E, P> {
            private final Set<EventType<P, ?, ?>> eventTypes;
            private final int                     mask;

            @SafeVarargs
            protected Abstract(EventType<P, ?, ?>... accepted) {
                this.eventTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(accepted)));
                this.mask       = computeMask();
            }

            protected int computeMask() {
                int yield = Bitmask.EMPTY;
                for (EventType<P, ?, ?> type : eventTypes) {
                    yield = combine(yield, type.getMask());
                }
                return yield;
            }

            @Override
            public boolean canAccept(EventType<P, ?, ?> eventType) {
                return eventTypes.contains(eventType);
            }

            @Override
            public int getAcceptedTypesAsMask() {
                return mask;
            }
        }

        static final class OfSortedInvocables<E extends EventType<? super P, ?, ?>, P extends Event<? super P>> extends Abstract<E, P> {
            private final Set<Invocable<Object>> invocables;

            OfSortedInvocables(
                    EventType<P, ?, ?>[] capabilities, Set<Invocable<Object>> invocables
            ) {
                super(capabilities);
                this.invocables = invocables;
            }

            @Override
            public <T extends P> void acceptEvent(T eventPayload) {
                final Class<T> payloadClass = deadCast(eventPayload.getClass());
                invocables.stream()
                        .filter(invocable -> invocable.typeOrder()[0].isAssignableFrom(payloadClass))
                        .forEachOrdered(invocable -> invocable.invokeRethrow(eventPayload));
            }
        }

        private static final class OfInvocable<E extends EventType<? super P, ?, ?>, P extends Event<? super P>> extends Abstract<E, P> {
            private final Invocable<? extends P> underlying;

            private OfInvocable(Invocable<? extends P> underlying) {
                this.underlying = underlying;
            }

            @Override
            public <T extends P> void acceptEvent(T eventPayload) {
                underlying.invokeRethrow(eventPayload);
            }
        }
    }
}
