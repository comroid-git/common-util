package org.comroid.listnr;

import org.comroid.common.func.Invocable;
import org.comroid.common.util.BitmaskUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.comroid.common.util.BitmaskUtil.combine;

public interface EventAcceptor {
    Set<EventType> getAcceptedEventTypes();

    @Internal
    int getAcceptedTypesAsMask();

    @Internal
    <P extends EventPayload> void acceptEvent(P eventPayload);

    static EventAcceptor ofMethod(Method method) {
        if (!method.isAnnotationPresent(EventHandler.class))
            throw new IllegalArgumentException("EventHandler annotation not present");
        final EventHandler handler = method.getAnnotation(EventHandler.class);

        return new Support.OfInvocable(Invocable.ofMethodCall(method));
    }

    final class Support {
        protected static abstract class Abstract implements EventAcceptor {
            private final Set<EventType> eventTypes;
            private final int            mask;

            protected Abstract(EventType... accepted) {
                this.eventTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(accepted)));
                this.mask       = computeMask();
            }

            @Override
            public Set<EventType> getAcceptedEventTypes() {
                return eventTypes;
            }

            @Override
            public int getAcceptedTypesAsMask() {
                return mask;
            }

            protected int computeMask() {
                int yield = BitmaskUtil.EMPTY;
                for (EventType type : eventTypes)
                    yield = combine(yield, type.getFlag());
                return yield;
            }
        }

        private static final class OfInvocable extends Abstract {
            private final Invocable<? extends EventPayload> underlying;

            private OfInvocable(Invocable<? extends EventPayload> underlying) {
                this.underlying = underlying;
            }

            @Override
            public <P extends EventPayload> void acceptEvent(P eventPayload) {

            }
        }
    }
}
