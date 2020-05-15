package org.comroid.listnr;

import org.comroid.common.Polyfill;
import org.comroid.common.info.Dependent;
import org.comroid.common.info.ExecutorBound;
import org.comroid.listnr.model.EventPayload;
import org.comroid.listnr.model.EventType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public abstract class ListnrCore<IN, D, MT extends EventType<IN, D, ? super MP>, MP extends EventPayload<D, ? super MT>>
        implements Dependent<D>, ExecutorBound {
    private final Collection<? extends MT> types = new ArrayList<>();
    private final Map<Listnr.Attachable<IN, D, ? super MT, ? super MP>, EventConsumers> consumers = new ConcurrentHashMap<>();
    private final Class<IN> inClass;
    private final D dependent;
    private final Executor executor;

    @Override
    public Executor getExecutor() {
        return executor;
    }

    public Collection<? extends MT> getRegisteredEventTypes() {
        return types;
    }

    @Override
    public @Nullable D getDependent() {
        return dependent;
    }

    /**
     * @param inTypeClass Type class to represent the IN parameter.
     * @param dependent   The dependency object.
     */
    protected ListnrCore(Class<IN> inTypeClass, D dependent) {
        this(ForkJoinPool.commonPool(), inTypeClass, dependent);
    }

    protected ListnrCore(Executor executor, Class<IN> inTypeClass, D dependent) {
        this.executor = executor;
        this.inClass = inTypeClass;
        this.dependent = dependent;
    }

    public <ET extends MT> void register(ET type) {
        types.add(Polyfill.uncheckedCast(type));
    }

    @Internal
    <EP extends MP> Runnable listen(final Listnr.Attachable<IN, D, ? super MT, ? super MP> listener,
                                    final EventType<IN, D, ? extends EP> eventType,
                                    final Consumer<EP> payloadConsumer) {
        synchronized (listener) {
            consumers(listener, eventType).add(payloadConsumer);

            return () -> consumers(listener, eventType).remove(payloadConsumer);
        }
    }

    @Internal
    public <ET extends EventType<IN, D, EventPayload<D, ET>>> void publish(
            final Listnr.Attachable<IN, D, ? super MT, ? super MP> attachable,
            final ET eventType,
            final Object[] data
    ) {
        final EventPayload<D, ET> payload = eventType.makePayload(Arrays.stream(data)
                .filter(inClass::isInstance)
                .findAny()
                .map(inClass::cast)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("No Input data {type = %s} found in array: %s",
                                inClass.getSimpleName(), Arrays.toString(data))
                )));

        getExecutor().execute(() -> consumers(attachable, Polyfill.uncheckedCast(eventType))
                .forEach(consumer -> consumer.accept(Polyfill.uncheckedCast(payload))));
    }

    private Collection<Consumer<? extends MP>> consumers(
            Listnr.Attachable<IN, D, ? super MT, ? super MP> attachable,
            EventType<IN, D, ? extends MP> type) {
        return consumers.computeIfAbsent(attachable, EventConsumers::new)
                .computeIfAbsent(type, key -> new ArrayList<>());
    }

    private class EventConsumers extends ConcurrentHashMap<EventType<IN, D, ? extends MP>, Collection<Consumer<? extends MP>>> {
        @SuppressWarnings("FieldCanBeLocal")
        private final Listnr.Attachable<IN, D, ? super MT, ? super MP> owner;

        public EventConsumers(Listnr.Attachable<IN, D, ? super MT, ? super MP> owner) {
            this.owner = owner;
        }
    }
}
