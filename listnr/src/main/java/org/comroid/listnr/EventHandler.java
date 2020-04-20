package org.comroid.listnr;

import java.util.concurrent.CompletableFuture;

public interface EventHandler<E extends Event<?>> {
    void handle(E event);

    interface API<S extends EventSender<S, ? extends E>, E extends Event<?>> {
        HandlerManager<S, E> always(EventHandler<E> handler);

        CompletableFuture<E> once();
    }
}
