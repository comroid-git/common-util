package org.comroid.listnr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.comroid.common.func.Invocable;
import org.comroid.common.func.ParamFactory;
import org.comroid.common.iter.Span;
import org.comroid.common.util.BitmaskUtil;

public final class EventHub<I, O> {
    private final Span<EventType<?, I, O>>  registeredTypes     = new Span<>();
    private final Span<EventAcceptor<?, ?>> registeredAcceptors = new Span<>();
    private final ScheduledExecutorService  executorService;
    private final Function<I, O>            preprocessor;

    public EventHub(
            ScheduledExecutorService executorService, Function<I, O> preprocessor
    ) {
        this.executorService = executorService;
        this.preprocessor    = preprocessor;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public <R, E extends Event<E>, T extends EventType<E, O, R>> EventHub<O, R> dependentHub(
            ScheduledExecutorService executorService,
            Function<O, R> preprocessor
    ) {
        class DependentHub extends EventAcceptor.Support.Abstract<T, E> {
            final EventHub<O, R> dependent = new EventHub<>(executorService, preprocessor);

            DependentHub() {
                super();
            }

            @Override
            public <P extends E> void acceptEvent(P eventPayload) {
                dependent.publish(eventPayload);
            }
        }

        return new DependentHub().dependent;
    }

    public <P extends Event<P>> void publish(final P eventPayload) {
        getRegisteredAcceptors().stream()
                .filter(acceptor -> BitmaskUtil.isFlagSet(acceptor.getAcceptedTypesAsMask(), eventPayload.getEventMask()))
                .map(it -> {//noinspection unchecked
                    return (EventAcceptor<? extends EventType<P, I, ?>, P>) it;
                })
                .map(acceptor -> (Runnable) () -> acceptor.acceptEvent(eventPayload))
                .forEachOrdered(executorService::execute);
    }

    public Collection<EventAcceptor<?, ?>> getRegisteredAcceptors() {
        return Collections.unmodifiableCollection(registeredAcceptors);
    }

    public <P extends Event<P>> EventType<P, I, O> createEventType(
            Class<P> payloadType, ParamFactory<O, P> payloadFactory, Predicate<O> eventTester
    ) {
        return new EventType.Support.Basic<>(this, payloadType, eventTester, payloadFactory);
    }

    public void registerEventType(EventType<?, I, O> type) {
        registeredTypes.add(type);
    }

    public <P extends Event<P>> void publish(I data) {
        final O out = preprocessor.apply(data);

        //noinspection unchecked
        EventType<? super P, I, O>[] subtypes = (EventType<? super P, I, O>[]) getRegisteredEventTypes().stream()
                .filter(type -> type.isEvent(out))
                .toArray();

        //noinspection unchecked
        publish((EventType<P, I, O>) subtypes[0], subtypes, out);
    }

    public Span<EventType<?, I, O>> getRegisteredEventTypes() {
        return registeredTypes;
    }

    private <P extends Event<P>> void publish(
            EventType<P, I, O> supertype, EventType<? super P, I, O>[] types, O data
    ) {
        if (types.length == 1) {
            //noinspection unchecked
            publish((P) types[0].create(data));
        } else {
            EventType.Combined<P, I, O> combined = EventType.Combined.of(supertype.payloadType(), supertype::isEvent, types);
            publish(combined.create(data));
        }
    }

    public <P extends Event<P>> void publish(EventType<P, I, O> asSupertype, O data) {
        //noinspection unchecked
        EventType<? super P, I, O>[] subtypes = (EventType<? super P, I, O>[]) getRegisteredEventTypes().stream()
                .filter(type -> BitmaskUtil.isFlagSet(asSupertype.getMask(), type.getMask()))
                .toArray();

        publish(asSupertype, subtypes, data);
    }

    public <E extends EventType<P, I, O>, P extends Event<P>> ListnrManager<I, O, E, P> registerAcceptor(
            EventAcceptor<E, P> acceptor
    ) {
        registeredAcceptors.add(acceptor);
        return new ListnrManager<>(this, acceptor);
    }

    public <E extends EventType<P, I, O>, P extends Event<P>> boolean unregisterAcceptor(
            EventAcceptor<E, P> acceptor
    ) {
        return registeredAcceptors.remove(acceptor);
    }

    public <T, E extends EventType<P, I, O>, P extends Event<P>> EventAcceptor<E, P> acceptorOfClass(
            Class<T> klass, T instance
    ) {
        final List<Method> useMethods = Arrays.stream(klass.getMethods())
                .filter(method -> method.isAnnotationPresent(Listnr.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == 1)
                .collect(Collectors.toList());
        //noinspection unchecked
        final EventType<P, I, O>[] capabilities = (EventType<P, I, O>[]) useMethods.stream()
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
