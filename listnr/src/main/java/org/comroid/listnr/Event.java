package org.comroid.listnr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.comroid.common.annotation.vanity.inheritance.ShouldExtend;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.common.util.BitmaskUtil;

import org.jetbrains.annotations.ApiStatus.Internal;

import static org.comroid.common.util.BitmaskUtil.combine;

@ShouldExtend(Event.Support.Abstract.class)
public interface Event<S extends Event<S>> extends SelfDeclared<S> {
    final class Support {
        public static abstract class Abstract<S extends Event<S>> implements Event<S> {
            private final Set<EventType<S, ?>> eventTypes;
            private final int                  mask;

            @SafeVarargs
            protected Abstract(EventType<S, ?>... subtypes) {
                this.eventTypes
                          = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(subtypes)));
                this.mask = computeMask();
            }

            protected int computeMask() {
                int yield = BitmaskUtil.EMPTY;
                for (EventType<S, ?> type : eventTypes) {
                    yield = combine(yield, type.getMask());
                }
                return yield;
            }

            @Override
            public Set<EventType<S, ?>> getEventTypes() {
                return eventTypes;
            }

            @Override
            public int getEventMask() {
                return mask;
            }
        }
    }

    Set<EventType<S, ?>> getEventTypes();

    @Internal
    int getEventMask();
}
