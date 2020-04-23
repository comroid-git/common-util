package org.comroid.listnr;

import java.util.concurrent.ExecutorService;
import java.util.function.ToIntFunction;

import org.comroid.common.func.ParamFactory;
import org.comroid.common.iter.Span;
import org.comroid.common.util.BitmaskUtil;

public final class EventHub<TF> {
    private final Span<EventType<?, TF>>        registeredTypes     = new Span<>();
    private final Span<? extends EventAcceptor> registeredAcceptors = new Span<>();
    private final ExecutorService               executorService;
    private final ToIntFunction<TF>             typeRewiringFunction;

    public EventHub(ExecutorService executorService, ToIntFunction<TF> typeRewiringFunction) {
        this.executorService      = executorService;
        this.typeRewiringFunction = typeRewiringFunction;
    }

    public <P extends Event<P>> EventType<P, TF> createEventType(ParamFactory<TF, P> payloadFactory) {
        return new EventType.Support.Basic<>(this, payloadFactory);
    }

    public void registerEventType(EventType<?, TF> type) {
        registeredTypes.add(type);
    }

    public <P extends Event<P>> void publish(EventType<P, TF> asSupertype, TF data) {
        getRegisteredEventTypes().stream()
                .filter(type -> BitmaskUtil.isFlagSet(type.getFlag(), asSupertype.getFlag()))
                .map(it -> {//noinspection unchecked
                    return (EventType<P, TF>) it;
                })
                .forEachOrdered(subtype -> {
                    publish(subtype.create(data));
                });
    }

    public Span<EventType<?, TF>> getRegisteredEventTypes() {
        return registeredTypes;
    }

    public <P extends Event<P>> void publish(final P eventPayload) {
        getRegisteredAcceptors().stream()
                .filter(acceptor -> {
                    return BitmaskUtil.isFlagSet(acceptor.getAcceptedTypesAsMask(),
                            eventPayload.getEventMask()
                    );
                })
                .map(acceptor -> (Runnable) () -> acceptor.acceptEvent(eventPayload))
                .forEachOrdered(executorService::execute);
    }

    public Span<? extends EventAcceptor> getRegisteredAcceptors() {
        return registeredAcceptors;
    }
}
