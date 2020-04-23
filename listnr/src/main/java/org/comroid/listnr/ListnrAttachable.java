package org.comroid.listnr;

import java.util.function.Consumer;

import org.comroid.common.util.BitmaskUtil;

public interface ListnrAttachable<TF, E extends EventType<P, TF>, P extends Event<P>> {
    default ListnrManager<TF, E, P> registerListener(Object listener) {
        //noinspection unchecked
        EventAcceptor<E, P> acceptorOfClass
                = getEventHub().acceptorOfClass((Class<Object>) listener.getClass(), listener);
        return getEventHub().registerAcceptor(acceptorOfClass);
    }

    EventHub<TF> getEventHub();

    default <ET extends EventType<T, TF>, T extends Event<T>> ListnrManager<TF, ET, T> listenTo(
            EventType<T, TF> type, Consumer<T> listener
    ) {
        if (!BitmaskUtil.isFlagSet(type.getMask(), getAcceptedType().getMask())) {
            throw new IllegalArgumentException(String.format(
                    "Cannot listen to type %s, only subtypes of %s allowed",
                    type,
                    getAcceptedType()
            ));
        }

        return getEventHub().registerAcceptor(EventAcceptor.ofConsumer(type.payloadType(),
                listener
        ));
    }

    E getAcceptedType();
}
