package org.comroid.listnr;

import org.comroid.api.Disposable;
import org.comroid.api.Polyfill;
import org.comroid.mutatio.pipe.Pipeable;
import org.comroid.listnr.model.EventPayload;
import org.comroid.listnr.model.EventType;
import org.comroid.mutatio.pump.Pump;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.comroid.api.Polyfill.uncheckedCast;

public @interface Listnr {
    interface Attachable<IN, D,
            MT extends EventType<IN, D, ? extends MT, ? extends MP>,
            MP extends EventPayload<D, ? extends MT, ? extends MP>> {
        ListnrCore<IN, D, ? super MT, ? super MP> getListnrCore();

        default <ET extends MT, EP extends MP> Listnr.API<IN, D, MT, MP, ET, EP> listenTo(ET eventType)
                throws IllegalArgumentException {
            return new Listnr.API<>(this, eventType);
        }

        default <ET extends MT, EP extends MP> void publish(ET eventType, Object... data) {
            hasEventTypeManaged(eventType);

            final ListnrCore<IN, D, ? super MT, ? super MP> listnrCore = this.getListnrCore();
            listnrCore.publish(this, eventType, data);
        }

        @Internal
        default <ET extends MT, EP extends MP> void hasEventTypeManaged(ET eventType) {
            if (!getListnrCore().getRegisteredEventTypes().contains(eventType))
                throw new IllegalArgumentException(String.format("Type %s is not managed by %s", eventType, this));
        }
    }

    final class API<IN, D,
            MT extends EventType<IN, D, ? extends MT, ? extends MP>, // main event type
            MP extends EventPayload<D, ? extends MT, ? extends MP>, // main event payload
            ET extends MT, EP extends MP> // this events information
            implements Pipeable<EP> {
        private final Attachable<IN, D, MT, MP> attachable;
        private final ET eventType;

        private API(Attachable<IN, D, MT, MP> attachable, ET eventType) {
            attachable.hasEventTypeManaged(eventType);

            this.attachable = attachable;
            this.eventType = eventType;
        }

        /**
         * Listens directly to published data.
         *
         * @param payloadConsumer The handler for the incoming payloads.
         * @return A runnable that will detach the handler.
         */
        public final Runnable directly(Consumer<EP> payloadConsumer) {
            return attachable.getListnrCore().<ET, EP>listen(attachable, uncheckedCast(eventType), payloadConsumer);
        }

        @Override
        public Pump<?, EP> pipe() {
            return pump();
        }

        /**
         * Listens to data and publishes all of it to a {@link Pump}
         * <p>
         * The returned {@linkplain Disposable Pump} can be
         * {@linkplain AutoCloseable#close() closed} in order to detach it from this ListnrAttachable
         *
         * @return A pump that will be filled with payloads
         */
        @Override
        public final Pump<?, EP> pump() {
            final Pump<EP, EP> pump = Pump.create();
            final Runnable detacher = directly(pump);
            pump.addChildren(detacher::run);

            return pump;
        }

        /**
         * Listens to data once and then detaches the consumer
         *
         * @return A future to contain the first received data
         */
        public final CompletableFuture<EP> once() {
            class FutureCompleter implements Consumer<EP> {
                private final CompletableFuture<EP> future = new CompletableFuture<>();

                @Override
                public synchronized void accept(EP p) {
                    if (future.isDone())
                        throw new IllegalStateException("Future is already completed");

                    future.complete(p);
                }
            }

            final FutureCompleter completer = new FutureCompleter();
            final Runnable detacher = directly(completer);
            completer.future.thenRunAsync(detacher, Runnable::run)
                    .exceptionally(Polyfill.exceptionLogger());

            return completer.future;
        }
    }
}
