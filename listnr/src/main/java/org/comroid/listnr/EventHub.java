package org.comroid.listnr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.comroid.common.func.Invocable;
import org.comroid.common.func.ParamFactory;
import org.comroid.common.iter.Span;
import org.comroid.common.util.BitmaskUtil;

public final class EventHub<TF> {
    public <EX extends ExecutorService & ScheduledExecutorService> EX getExecutorService() {
        //noinspection unchecked
        return (EX) executorService;
    }

    private final Span<EventType<?, TF>>    registeredTypes     = new Span<>();
    private final Span<EventAcceptor<?, ?>> registeredAcceptors = new Span<>();
    private final ExecutorService           executorService;

    public <EX extends ExecutorService & ScheduledExecutorService> EventHub(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public <P extends Event<P>> EventType<P, TF> createEventType(
            Class<P> payloadType, ParamFactory<TF, P> payloadFactory, Predicate<TF> eventTester
    ) {
        return new EventType.Support.Basic<>(this, payloadType, eventTester, payloadFactory);
    }

    public void registerEventType(EventType<?, TF> type) {
        registeredTypes.add(type);
    }

    public <P extends Event<P>> void publish(TF data) {
        //noinspection unchecked
        EventType<? super P, TF>[] subtypes = (EventType<? super P, TF>[]) getRegisteredEventTypes().stream()
                .filter(type -> type.isEvent(data))
                .toArray();

        //noinspection unchecked
        publish((EventType<P, TF>) subtypes[0], subtypes, data);
    }

    public Span<EventType<?, TF>> getRegisteredEventTypes() {
        return registeredTypes;
    }

    private <P extends Event<P>> void publish(
            EventType<P, TF> supertype, EventType<? super P, TF>[] types, TF data
    ) {
        if (types.length == 1) {
            //noinspection unchecked
            publish((P) types[0].create(data));
        } else {
            EventType.Combined<P, TF> combined = EventType.Combined.of(supertype.payloadType(), supertype::isEvent, types);
            publish(combined.create(data));
        }
    }

    public <P extends Event<P>> void publish(final P eventPayload) {
        getRegisteredAcceptors().stream()
                .filter(acceptor -> BitmaskUtil.isFlagSet(acceptor.getAcceptedTypesAsMask(), eventPayload.getEventMask()))
                .map(it -> {//noinspection unchecked
                    return (EventAcceptor<? extends EventType<P, ?>, P>) it;
                })
                .map(acceptor -> (Runnable) () -> acceptor.acceptEvent(eventPayload))
                .forEachOrdered(executorService::execute);
    }

    public Collection<EventAcceptor<?, ?>> getRegisteredAcceptors() {
        return Collections.unmodifiableCollection(registeredAcceptors);
    }

    public <P extends Event<P>> void publish(EventType<P, TF> asSupertype, TF data) {
        //noinspection unchecked
        EventType<? super P, TF>[] subtypes = (EventType<? super P, TF>[]) getRegisteredEventTypes().stream()
                .filter(type -> BitmaskUtil.isFlagSet(asSupertype.getMask(), type.getMask()))
                .toArray();

        publish(asSupertype, subtypes, data);
    }

    public <E extends EventType<P, ?>, P extends Event<P>> ListnrManager<TF, E, P> registerAcceptor(
            EventAcceptor<E, P> acceptor
    ) {
        registeredAcceptors.add(acceptor);
        return new ListnrManager<>(this, acceptor);
    }

    public <E extends EventType<P, ?>, P extends Event<P>> boolean unregisterAcceptor(
            EventAcceptor<E, P> acceptor
    ) {
        return registeredAcceptors.remove(acceptor);
    }

    public <T, E extends EventType<P, ?>, P extends Event<P>> EventAcceptor<E, P> acceptorOfClass(
            Class<T> klass, T instance
    ) {
        final List<Method> useMethods = Arrays.stream(klass.getMethods())
                .filter(method -> method.isAnnotationPresent(Listnr.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == 1)
                .collect(Collectors.toList());
        //noinspection unchecked
        final EventType<P, ?>[] capabilities = (EventType<P, ?>[]) useMethods.stream()
                .map(method -> method.getParameterTypes()[0])
                .filter(type -> getRegisteredEventTypes().stream()
                        .map(EventType::payloadType)
                        .anyMatch(type::isAssignableFrom))
                .toArray();
        final Set<Invocable<Object>> invocables = useMethods.stream()
                .map(method -> Invocable.ofMethodCall(method, instance))
                .collect(Collectors.toSet());

        return new EventAcceptor.Support.OfSortedInvocables<>(capabilities, invocables);
    }
}