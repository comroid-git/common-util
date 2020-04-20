package org.comroid.listnr;

import org.comroid.common.iter.Span;

import java.util.concurrent.ExecutorService;
import java.util.function.ToIntFunction;

public final class EventHub<TF> {
    private final Span<EventType>               registeredTypes     = new Span<>();
    private final Span<? extends EventAcceptor> registeredAcceptors = new Span<>();
    private final ExecutorService               executorService;
    private final ToIntFunction<TF>             typeRewiringFunction;

    public EventHub(ExecutorService executorService, ToIntFunction<TF> typeRewiringFunction) {
        this.executorService      = executorService;
        this.typeRewiringFunction = typeRewiringFunction;
    }

    public EventType<TF> createEventType() {
        return new EventType.Support.Basic<>(this);
    }

    public void registerEventType(EventType<TF> type) {
        registeredTypes.add(type);
    }

    public Span<EventType> getRegisteredEventTypes() {
        return registeredTypes;
    }

    public Span<? extends EventAcceptor> getRegisteredAcceptors() {
        return registeredAcceptors;
    }

    public <E extends EventBase> void publish(E event) {

    }
}
