package org.comroid.listnr;

import java.util.concurrent.ExecutorService;
import java.util.function.ToIntFunction;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.iter.Span;
import org.comroid.common.util.BitmaskUtil;

public final class EventHub<TF> {
    private final Span<EventType<?, TF>>              registeredTypes     = new Span<>();
    private final Span<? extends EventAcceptor<?, ?>> registeredAcceptors = new Span<>();
    private final ExecutorService                     executorService;

    public EventHub(ExecutorService executorService, ToIntFunction<TF> typeRewiringFunction) {
        this.executorService = executorService;
    }

    public <P extends Event<P>> EventType<P, TF> createEventType(
            Class<P> payloadType, ParamFactory<TF, P> payloadFactory
    ) {
        return new EventType.Support.Basic<>(this, payloadType, payloadFactory);
    }

    public void registerEventType(EventType<?, TF> type) {
        registeredTypes.add(type);
    }

    public <P extends Event<P>> void publish(EventType<P, TF> asSupertype, TF data) {
        //noinspection unchecked
        EventType<? super P, TF>[] subtypes
                = (EventType<? super P, TF>[]) getRegisteredEventTypes().stream()
                .filter(type -> BitmaskUtil.isFlagSet(asSupertype.getMask(), type.getMask()))
                .toArray();
        EventType.Combined<P, TF> combined = EventType.Combined.of(asSupertype.payloadType(),
                subtypes
        );
        publish(combined.create(data));
    }

    public Span<EventType<?, TF>> getRegisteredEventTypes() {
        return registeredTypes;
    }

    public <P extends Event<P>> void publish(final P eventPayload) {
        getRegisteredAcceptors().stream()
                .filter(acceptor -> BitmaskUtil.isFlagSet(acceptor.getAcceptedTypesAsMask(),
                        eventPayload.getEventMask()
                ))
                .map(it -> {//noinspection unchecked
                    return (EventAcceptor<? extends EventType<P, ?>, P>) it;
                })
                .map(acceptor -> (Runnable) () -> acceptor.acceptEvent(eventPayload))
                .forEachOrdered(executorService::execute);
    }

    public Span<? extends EventAcceptor<?, ?>> getRegisteredAcceptors() {
        return registeredAcceptors;
    }
}
