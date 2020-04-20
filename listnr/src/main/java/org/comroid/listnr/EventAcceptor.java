package org.comroid.listnr;

import org.comroid.common.util.BitmaskUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.Internal;

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
    <E extends EventPayload> void acceptEvent(E event);

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
    }
}
