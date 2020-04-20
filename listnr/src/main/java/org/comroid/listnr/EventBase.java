package org.comroid.listnr;

import org.comroid.common.util.BitmaskUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.Set;

public interface EventBase {
    Set<EventType> getEventTypes();

    @Internal
    default int getEventMask() {
        return getEventTypes()
                .stream()
                .mapToInt(EventType::getFlag)
                .collect(BitmaskUtil.collectMask());
    }

    final class Support {
        protected static abstract class AbstractEventBase {

        }
    }
}
