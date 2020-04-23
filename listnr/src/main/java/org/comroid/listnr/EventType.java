package org.comroid.listnr;

import org.comroid.common.annotation.vanity.inheritance.MustExtend;
import org.comroid.common.func.ParamFactory;
import org.comroid.common.util.BitmaskUtil;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

@MustExtend(EventType.Support.Basic.class)
public interface EventType<P extends Event<P>, TF> extends ParamFactory<TF, P> {
    final class Support {
        public static class Basic<P extends Event<P>, TF> implements EventType<P, TF> {
            protected final EventHub<TF>        hub;
            private final   int                 flag = BitmaskUtil.nextFlag();
            private final   ParamFactory<TF, P> payloadFactory;

            public Basic(EventHub<TF> hub, ParamFactory<TF, P> payloadFactory) {
                this.hub            = hub;
                this.payloadFactory = payloadFactory;

                hub.registerEventType(this);
            }

            @Override
            public int getFlag() {
                return flag;
            }

            @Override
            public P create(@Nullable TF parameter) {
                return payloadFactory.create(parameter);
            }

            @Override
            public int counter() {
                return payloadFactory.counter();
            }

            @Override
            public int peekCounter() {
                return payloadFactory.peekCounter();
            }
        }
    }

    @Internal
    int getFlag();

    @Override
    P create(@Nullable TF parameter);
}
