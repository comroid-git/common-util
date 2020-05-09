package old;

import java.util.function.Consumer;

import org.comroid.common.util.Bitmask;

public interface ListnrAttachable<I, O, E extends EventType<? super I, ? super O, ? super P>, P extends Event<? super P>> {
    default ListnrManager<I, O, E, P> registerListener(Object listener) {
        //noinspection unchecked
        EventAcceptor<E, P> acceptorOfClass = getEventHub().acceptorOfClass((Class<Object>) listener.getClass(), listener);
        return getEventHub().registerAcceptor(acceptorOfClass);
    }

    EventHub<I, O, E, P> getEventHub();

    default <ET extends EventType<I, O, T>, T extends Event<T>> ListnrManager<I, O, ET, T> listenTo(
            EventType<I, O, T> type, Consumer<T> listener
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
