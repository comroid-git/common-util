package org.comroid.uniform.adapter.http.jdk;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.logging.Level;

import org.comroid.restless.socket.WebSocketEvent;

import com.google.common.flogger.FluentLogger;

public class JavaWebSocket<O, E extends WebSocketEvent<? super E>> extends org.comroid.restless.socket.WebSocket<O, E> {
    final WebSocket.Listener           javaListener = new Listener();
    final CompletableFuture<WebSocket> socket       = new CompletableFuture<>();

    protected JavaWebSocket(ThreadGroup threadGroup, Function<String, O> preprocessor) {
        super(threadGroup, preprocessor);
    }

    @Override
    protected CompletableFuture<Void> sendString(String node, boolean last) {
        return socket.thenCompose(socket -> socket.sendText(node, last))
                .thenApply(nil -> null);
    }

    @Override
    public IntFunction<String> getCloseCodeResolver() {
        return closeCodeResolver;
    }

    @Override
    public void setCloseCodeResolver(IntFunction<String> closeCodeResolver) {
        this.closeCodeResolver = closeCodeResolver;
    }

    public IntFunction<String> closeCodeResolver = String::valueOf;

    private final class Listener implements WebSocket.Listener {
        private final FluentLogger logger = FluentLogger.forEnclosingClass();

        @Override
        public void onOpen(WebSocket webSocket) {
            logger.at(Level.FINE)
                    .log("WebSocket %s opened", JavaWebSocket.this.toString());
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            sb.append(data);

            if (last) {
                getEventHub().publish(sb.toString());
                sb = new StringBuilder();
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            sb.append(data.asCharBuffer()
                    .toString());

            if (last) {
                getEventHub().publish(sb.toString());
                sb = new StringBuilder();
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
            webSocket.sendPong(message);

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
            webSocket.sendPing(message);

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            logger.at(Level.INFO)
                    .log("WebSocket closed with code %s and reason %s", closeCodeResolver.apply(statusCode), reason);

            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            logger.at(Level.SEVERE)
                    .withCause(error)
                    .log("WebSocket encountered an error");

            webSocket.request(1);
        }

        private StringBuilder sb = new StringBuilder();
    }
}
