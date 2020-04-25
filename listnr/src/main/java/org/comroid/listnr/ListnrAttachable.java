package org.comroid.listnr;

import java.util.function.Consumer;

import org.comroid.common.util.Bitmask;

public interface ListnrAttachable<I, O, E extends EventType<? super P, ? super I, ? super O>, P extends Event<P>> {
    default ListnrManager<I, O, E, P> registerListener(Object listener) {
        //noinspection unchecked
        EventAcceptor<E, P> acceptorOfClass = getEventHub().acceptorOfClass((Class<Object>) listener.getClass(), listener);
        return getEventHub().registerAcceptor(acceptorOfClass);
    }

    EventHub<I, O> getEventHub();

    default <ET extends EventType<T, I, O>, T extends Event<T>> ListnrManager<I, O, ET, T> listenTo(
            EventType<T, I, O> type, Consumer<T> listener
    ) {
        if (!Bitmask.isFlagSet(type.getMask(), getBaseEventType().getMask())) {
            throw new IllegalArgumentException(String.format("Cannot listen to type %s, only subtypes of %s allowed",
                    type,
                    getBaseEventType()
            ));
        }

        return getEventHub().registerAcceptor(EventAcceptor.ofConsumer(type.payloadType(), listener));
    }

    E getBaseEventType();
}
