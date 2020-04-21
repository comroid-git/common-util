package org.comroid.listnr;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.util.BitmaskUtil;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface EventType<P extends EventPayload, TF> {
    @Internal
    int getFlag();

    ParamFactory<TF, P> payloadFactory();

    final class Support {
        protected static final class Basic<P extends EventPayload, TF> implements EventType<P, TF> {
            private final int                 flag = BitmaskUtil.nextFlag();
            private final EventHub            hub;
            private final ParamFactory<TF, P> payloadFactory;

            protected Basic(EventHub hub, ParamFactory<TF, P> payloadFactory) {
                this.hub            = hub;
                this.payloadFactory = payloadFactory;

                hub.registerEventType(this);
            }

            @Override
            public int getFlag() {
                return flag;
            }

            @Override
            public ParamFactory<TF, P> payloadFactory() {
                return payloadFactory;
            }
        }
    }
}
