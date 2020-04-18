package org.comroid.listnr;

import org.comroid.common.iter.Span;
import org.comroid.common.ref.SelfDeclared;
import org.comroid.dreadpool.ThreadPool;

public interface EventSender<Self extends EventSender<Self, ? extends E>, E extends Event<Self>> extends SelfDeclared<Self> {
    ThreadPool getThreadPool();

    Span<HandlerManager<Self, ? extends E>> getAttachedManagers();

    <T extends E> EventHandler.API<? extends Self, T> attachHandler(T event);

    <T extends E> boolean detachManager(HandlerManager<Self, ? super E> manager);

    default int detachAll() {
        return (int) getAttachedManagers().stream()
                .filter(HandlerManager::detachNow)
                .count();
    }
}
