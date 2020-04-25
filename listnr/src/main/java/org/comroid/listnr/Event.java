package org.comroid.listnr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.comroid.common.annotation.vanity.inheritance.ShouldExtend;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.common.util.Bitmask;

import org.jetbrains.annotations.ApiStatus.Internal;

import static org.comroid.common.util.Bitmask.combine;

@ShouldExtend(Event.Support.Abstract.class)
public interface Event<S extends Event<? super S>> extends SelfDeclared<S> {
    Set<EventType<? extends S, ?, ?>> getEventTypes();

    @Internal
    int getEventMask();

    final class Support {
        public static abstract class Abstract<S extends Event<? super S>> implements Event<S> {
            private final Set<EventType<? extends S, ?, ?>> eventTypes;
            private final int                               mask;

            @SafeVarargs
            protected Abstract(EventType<? extends S, ?, ?>... subtypes) {
                this.eventTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(subtypes)));
                this.mask       = computeMask();
            }

            protected int computeMask() {
                int yield = Bitmask.EMPTY;
                for (EventType<?, ?, ?> type : eventTypes) {
                    yield = combine(yield, type.getMask());
                }
                return yield;
            }

            @Override
            public Set<EventType<? extends S, ?, ?>> getEventTypes() {
                return eventTypes;
            }

            @Override
            public int getEventMask() {
                return mask;
            }
        }
    }
}
