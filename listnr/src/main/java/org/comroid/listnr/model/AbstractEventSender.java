package org.comroid.listnr.model;

import org.comroid.common.iter.Span;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.Event;
import org.comroid.listnr.EventHandler;
import org.comroid.listnr.EventSender;
import org.comroid.listnr.HandlerManager;

public class AbstractEventSender<Self extends EventSender<Self, ? extends E>, E extends Event<Self>> implements EventSender<Self, E> {
    private final ThreadPool                        threadPool;
    private final Span<HandlerManager<Self, ? extends E>> attached;

    public AbstractEventSender(ThreadPool threadPool) {
        this.threadPool = threadPool;
        this.attached = new Span<>();
    }

    @Override
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    @Override
    public Span<HandlerManager<Self, ? extends E>> getAttachedManagers() {
        return attached;
    }

    @Override
    public <T extends E> EventHandler.API<? extends Self, T> attachHandler(T event) {
        return (EventHandler.API<? extends Self, T>) new EventHandler.API<>(self(), event);
    }

    @Override
    public <T extends E> boolean detachManager(HandlerManager<Self, ? super E> manager) {
        return false;
    }
}
