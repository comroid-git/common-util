package org.comroid.listnr;

import org.comroid.common.util.BitmaskUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface EventType<TF> {
    @Internal
    int getFlag();

    final class Support {
        static final class Basic<TF> implements EventType<TF> {
            private final int           flag = BitmaskUtil.nextFlag();
            private final EventHub      hub;

            protected Basic(EventHub hub) {
                this.hub    = hub;

                hub.registerEventType(this);
            }

            @Override
            public int getFlag() {
                return flag;
            }
        }
    }
}
