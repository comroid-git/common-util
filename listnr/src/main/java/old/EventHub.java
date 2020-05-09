package old;

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

import org.comroid.common.Polyfill;
import org.comroid.common.func.Invocable;
import org.comroid.common.func.ParamFactory;
import org.comroid.common.iter.Span;
import org.comroid.common.util.Bitmask;

import static org.comroid.common.Polyfill.uncheckedCast;

public final class EventHub<I, O, E extends EventType<? super I, ? super O, ? super P>, P extends Event<? super P>>
        implements ListnrAttachable<I, O, E, P> {
    private final Span<EventType<I, O, ?>>  registeredTypes     = new Span<>();
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

    public <R, X extends Event<? super X>, T extends EventType<? super O, ? super R, ? super X>> EventHub<O, R, T, X> dependentHub(
            Function<O, R> preprocessor
    ) {
        return dependentHub(executorService, preprocessor);
    }

    public <R, X extends Event<? super X>, T extends EventType<? super O, ? super R, ? super X>> EventHub<O, R, T, X> dependentHub(
            ScheduledExecutorService executorService, Function<O, R> preprocessor
    ) {
        class DependentHubForwarder extends EventAcceptor.Support.Abstract<T, X> {
            final EventHub<O, R, T, X> dependent = new EventHub<>(executorService, preprocessor);

            DependentHubForwarder() {
                super();
            }

            @Override
            public boolean canAccept(EventType<?, ?, X> eventType) {
                return true;
            }

            @Override
            public <P extends X> void acceptEvent(P eventPayload) {
                dependent.publish(eventPayload);
            }
        }

        return new DependentHubForwarder().dependent;
    }

    public <P extends Event<? super P>> void publish(final P eventPayload) {
        getRegisteredAcceptors().stream()
                .filter(acceptor -> Bitmask.isFlagSet(acceptor.getAcceptedTypesAsMask(), eventPayload.getEventMask()))
                .map(it -> {//noinspection unchecked
                    return (EventAcceptor<? extends EventType<I, ?, P>, P>) it;
                })
                .map(acceptor -> (Runnable) () -> acceptor.acceptEvent(eventPayload))
                .forEachOrdered(executorService::execute);
    }

    public Collection<EventAcceptor<?, ?>> getRegisteredAcceptors() {
        return Collections.unmodifiableCollection(registeredAcceptors);
    }

    public <P extends Event<? super P>> EventType<I, O, P> createEventType(
            Class<P> payloadType, ParamFactory<O, P> payloadFactory, Predicate<O> eventTester
    ) {
        return new EventType.Support.Basic<>(uncheckedCast(this), payloadType, eventTester, payloadFactory);
    }

    public void registerEventType(EventType<I, O, ?> type) {
        registeredTypes.add(type);
    }

    public <P extends Event<? super P>> void publish(I data) {
        final O out = preprocessor.apply(data);

        //noinspection unchecked
        EventType<I, O, P>[] subtypes = (EventType<I, O, P>[]) getRegisteredEventTypes().stream()
                .filter(type -> type.isEvent(out))
                .toArray();

        publish(subtypes[0], subtypes, out);
    }

    public Span<EventType<I, O, ?>> getRegisteredEventTypes() {
        return registeredTypes;
    }

    private <P extends Event<? super P>> void publish(
            EventType<I, O, P> supertype, EventType<I, O, ? super P>[] types, O data
    ) {
        if (types.length == 1) {
            //noinspection unchecked
            publish((P) types[0].create(data));
        } else {
            EventType.Combined<P, I, O> combined = EventType.Combined.of(supertype.payloadType(), supertype::isEvent, types);
            publish(combined.create(data));
        }
    }

    @Override
    public EventHub<I, O, E, P> getEventHub() {
        return this;
    }

    @Override
    public E getBaseEventType() {
        return uncheckedCast(getRegisteredEventTypes().requireNonNull("No EventTypes registered"));
    }

    public <P extends Event<? super P>> void publish(EventType<I, O, P> asSupertype, O data) {
        //noinspection unchecked
        EventType<I, O, ? super P>[] subtypes = (EventType<I, O, ? super P>[]) getRegisteredEventTypes().stream()
                .filter(type -> Bitmask.isFlagSet(asSupertype.getMask(), type.getMask()))
                .toArray();

        publish(asSupertype, subtypes, data);
    }

    public <E extends EventType<? super I, ? super O, ? super P>, P extends Event<? super P>> ListnrManager<I, O, E, P> registerAcceptor(
            EventAcceptor<E, P> acceptor
    ) {
        registeredAcceptors.add(acceptor);
        return new ListnrManager<>(Polyfill.uncheckedCast(this), acceptor);
    }

    public <E extends EventType<? super I, ? super O, ? super P>, P extends Event<? super P>> boolean unregisterAcceptor(
            EventAcceptor<E, P> acceptor
    ) {
        return registeredAcceptors.remove(acceptor);
    }

    public <T, E extends EventType<? super I, ? super O, ? super P>, P extends Event<? super P>> EventAcceptor<E, P> acceptorOfClass(
            Class<T> klass, T instance
    ) {
        final List<Method> useMethods = Arrays.stream(klass.getMethods())
                .filter(method -> method.isAnnotationPresent(Listnr.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == 1)
                .collect(Collectors.toList());
        //noinspection unchecked
        final EventType<I, O, P>[] capabilities = (EventType<I, O, P>[]) useMethods.stream()
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
