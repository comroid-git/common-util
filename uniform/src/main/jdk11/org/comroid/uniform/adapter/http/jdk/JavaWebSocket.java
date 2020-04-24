package org.comroid.uniform.adapter.http.jdk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.comroid.uniform.SerializationAdapter;

import com.sun.net.httpserver.HttpExchange;

public class JavaWebSocket extends org.comroid.restless.socket.WebSocket<HttpExchange> {
    final WebSocket.Listener           javaListener = new Listener();
    final CompletableFuture<WebSocket> socket       = new CompletableFuture<>();

    public JavaWebSocket(ThreadGroup threadGroup, SerializationAdapter<?, ?, ?> serializationAdapter) {
        super(
                threadGroup,
                exchange -> serializationAdapter.createUniNode(new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines()
                        .collect(Collectors.joining()))
                        .asObjectNode()
        );
    }

    private final class Listener implements WebSocket.Listener {}
}
