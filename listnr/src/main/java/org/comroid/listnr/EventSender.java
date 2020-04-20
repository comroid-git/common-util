package org.comroid.listnr;

import org.comroid.common.iter.Span;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.dreadpool.ThreadPool;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface EventSender<Self extends EventSender<Self, ? extends E>, E extends CombinedEvent<?>>
        extends SelfDeclared<Self> {
    ThreadPool getThreadPool();

    Span<HandlerManager<Self, ? extends E>> getAttachedManagers();

    <T extends E, S extends EventSender<S, T>> EventHandler.API<S, T> attachHandler(T event);

    <T extends E> boolean detachManager(HandlerManager<Self, ? super E> manager);

    default int detachAll() {
        return (int) getAttachedManagers().stream()
                .filter(HandlerManager::detachNow)
                .count();
    }

    @Internal
    <TF, S extends EventSender<S, T>, T extends E> EventType<TF, S, T> createEventType();

    @Internal
    <T extends E> int sendEvent(T event);
}
