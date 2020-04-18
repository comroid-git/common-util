package org.comroid.listnr.model;

import org.comroid.common.iter.Span;
import org.comroid.dreadpool.ThreadPool;
import org.comroid.listnr.*;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractEventSender<Self extends EventSender<Self, ? extends E>, E extends Event<Self>>
        implements EventSender<Self, E> {
    private final ThreadPool                              threadPool;
    private final Span<HandlerManager<Self, ? extends E>> attached;

    public AbstractEventSender(ThreadPool threadPool) {
        this.threadPool = threadPool;
        this.attached   = new Span<>();
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
    public <T extends E, S extends EventSender<S, T>> EventHandler.API<S, T> attachHandler(T event) {
        return new BasicAPI<S, T>(self(), event);
    }

    @Override
    public <T extends E> boolean detachManager(HandlerManager<Self, ? super E> manager) {
        return false;
    }

    public class BasicAPI<S extends EventSender<S, E extends Event<S>>, T extends Event<?>> implements EventHandler.API<S, T> {
        private final S                  sender;
        private final EventType<?, S, T> event;

        public BasicAPI(S sender, EventType<?, S, T> event) {
            this.sender = sender;
            this.event  = event;
        }

        @Override
        public HandlerManager<S, T> always(EventHandler<T> handler) {
            return new AbstractHandlerManager<S, T>(sender) {};
        }

        @Override
        public CompletableFuture<T> once() {
            class Local implements EventHandler<T> {
                final CompletableFuture<T> future = new CompletableFuture<>();

                @Override
                public void handle(T event) {
                    if (future.isDone())
                        throw new IllegalStateException("Future already done!");

                    future.complete(event);
                }
            }

            final Local                local   = new Local();
            final HandlerManager<S, T> manager = always(local);
            local.future.thenRunAsync(() -> sender.detachManager(manager), sender.getThreadPool());
            return local.future;
        }
    }
}
