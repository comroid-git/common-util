package org.comroid.listnr;

import org.comroid.api.Polyfill;
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

public abstract class ListnrCore<IN, D, MT extends EventType<IN, D, ? extends MT, ? extends MP>, MP extends EventPayload<D, ? extends MT, ? extends MP>>
        implements Dependent<D>, ExecutorBound {
    private final Collection<? extends MT> types = new ArrayList<>();
    private final Map<Listnr.Attachable<IN, D, ? super MT, ? super MP>, EventConsumers<? super MT, ? super MP>> consumers
            = new ConcurrentHashMap<>();
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
    <ET extends MT, EP extends MP> Runnable listen(
            final Listnr.Attachable<IN, D, ? super ET, ? super EP> listener,
            final ET eventType,
            final Consumer<EP> payloadConsumer
    ) {
        synchronized (listener) {
            this.<ET, EP>consumers(listener, eventType).add(payloadConsumer);

            return () -> this.<ET, EP>consumers(listener, eventType).remove(payloadConsumer);
        }
    }

    @Internal
    public <ET extends MT, EP extends MP> void publish(
            final Listnr.Attachable<IN, D, ? super ET, ? super EP> attachable,
            final ET eventType,
            final Object[] data
    ) {
        //noinspection unchecked
        final EP payload = (EP) eventType.makePayload(Arrays.stream(data)
                .filter(inClass::isInstance)
                .findAny()
                .map(inClass::cast)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("No Input data {type = %s} found in array: %s",
                                inClass.getSimpleName(), Arrays.toString(data))
                )));

        getExecutor().execute(() -> this.<ET, EP>consumers(attachable, eventType)
                .forEach(consumer -> consumer.accept(payload)));
    }

    private <ET extends MT, EP extends MP> Collection<Consumer<? super EP>> consumers(
            Listnr.Attachable<IN, D, ? super ET, ? super EP> attachable,
            ET type) {
        //todo: whatever is to do here
        //noinspection Convert2MethodRef
        return Polyfill.<Map<ET, Collection<Consumer<? super EP>>>>uncheckedCast((consumers
                .computeIfAbsent(Polyfill.uncheckedCast(attachable), owner -> new EventConsumers<>(owner))))
                .computeIfAbsent(type, key -> new ArrayList<>());
    }

    private class EventConsumers<ET extends MT, EP extends MP> extends ConcurrentHashMap<ET, Collection<Consumer<? extends EP>>> {
        @SuppressWarnings("FieldCanBeLocal")
        private final Listnr.Attachable<IN, D, ? super ET, ? super EP> owner;

        public EventConsumers(Listnr.Attachable<IN, D, ? super ET, ? super EP> owner) {
            this.owner = owner;
        }
    }
}
