package org.comroid.uniform.adapter.http.jdk;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

import org.comroid.uniform.SerializationAdapter;

public class JavaWebSocket extends org.comroid.restless.socket.WebSocket {
    public JavaWebSocket(ThreadGroup threadGroup, SerializationAdapter<?, ?, ?> serializationAdapter) {
        super(threadGroup, preprocessor);
    }

    private final class Listener implements WebSocket.Listener {}
    final WebSocket.Listener           javaListener = new Listener();
    final CompletableFuture<WebSocket> socket       = new CompletableFuture<>();
}
