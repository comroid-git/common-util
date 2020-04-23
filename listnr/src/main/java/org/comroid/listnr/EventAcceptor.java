package org.comroid.listnr;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.comroid.common.func.Invocable;
import org.comroid.common.util.BitmaskUtil;

import org.jetbrains.annotations.ApiStatus.Internal;

import static org.comroid.common.util.BitmaskUtil.combine;

public interface EventAcceptor {
    static EventAcceptor ofMethod(Method method) {
        if (!method.isAnnotationPresent(EventHandler.class)) {
            throw new IllegalArgumentException("EventHandler annotation not present");
        }
        final EventHandler handler = method.getAnnotation(EventHandler.class);

        return new Support.OfInvocable(Invocable.ofMethodCall(method));
    }

    final class Support {
        protected static abstract class Abstract implements EventAcceptor {
            private final Set<EventType> eventTypes;
            private final int            mask;

            protected Abstract(EventType... accepted) {
                this.eventTypes =
                        Collections.unmodifiableSet(new HashSet<>(Arrays.asList(accepted)));
                this.mask       = computeMask();
            }

            protected int computeMask() {
                int yield = BitmaskUtil.EMPTY;
                for (EventType type : eventTypes) {
                    yield = combine(yield, type.getFlag());
                }
                return yield;
            }

            @Override
            public Set<EventType> getAcceptedEventTypes() {
                return eventTypes;
            }

            @Override
            public int getAcceptedTypesAsMask() {
                return mask;
            }
        }

        private static final class OfInvocable extends Abstract {
            private final Invocable<? extends Event> underlying;

            private OfInvocable(Invocable<? extends Event> underlying) {
                this.underlying = underlying;
            }

            @Override
            public <P extends Event> void acceptEvent(P eventPayload) {

            }
        }
    }

    Set<EventType> getAcceptedEventTypes();

    @Internal
    int getAcceptedTypesAsMask();

    @Internal
    <P extends Event> void acceptEvent(P eventPayload);
}
