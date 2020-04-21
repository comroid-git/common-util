package org.comroid.listnr;

import org.comroid.common.util.BitmaskUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.comroid.common.util.BitmaskUtil.combine;

public interface EventPayload {
    Set<EventType> getEventTypes();

    @Internal
    int getEventMask();

    final class Support {
        public static abstract class Abstract implements EventPayload {
            private final Set<EventType> eventTypes;
            private final int            mask;

            protected Abstract(EventType... subtypes) {
                this.eventTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(subtypes)));
                this.mask       = computeMask();
            }

            @Override
            public Set<EventType> getEventTypes() {
                return eventTypes;
            }

            @Override
            public int getEventMask() {
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
