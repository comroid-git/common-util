package org.comroid.uniform.adapter.http.jdk;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

import org.comroid.uniform.SerializationAdapter;

public class JavaWebSocket extends org.comroid.restless.socket.WebSocket {
    final WebSocket.Listener           javaListener = new Listener();
    final CompletableFuture<WebSocket> socket       = new CompletableFuture<>();

    public JavaWebSocket(ThreadGroup threadGroup, SerializationAdapter<?, ?, ?> serializationAdapter) {
        super(threadGroup);
    }

    private final class Listener implements WebSocket.Listener {}
}
