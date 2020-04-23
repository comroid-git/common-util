package org.comroid.listnr;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.util.BitmaskUtil;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

public interface EventType<P extends Event, TF> extends ParamFactory<TF, P> {
    final class Support {
        protected static final class Basic<P extends Event, TF> implements EventType<P, TF> {
            protected final EventHub<TF>        hub;
            private final   int                 flag = BitmaskUtil.nextFlag();
            private final   ParamFactory<TF, P> payloadFactory;

            protected Basic(EventHub<TF> hub, ParamFactory<TF, P> payloadFactory) {
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

    @Internal
    int getFlag();

    @Override
    P create(@Nullable TF parameter);
}
