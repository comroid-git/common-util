package org.comroid.listnr;

import org.comroid.listnr.model.AbstractEventSender;

import java.util.concurrent.CompletableFuture;

public interface EventHandler<E extends Event<?>> {
    void handle(E event);

    final class API<S extends EventSender<S, ? extends E>, E extends Event<S>> {
        public API(S sender, E event) {
        }

        public HandlerManager<S, E> always(EventHandler<E> handler) {
            return null;
        }

        public CompletableFuture<E> once() {
            return null;
        }
    }
}
