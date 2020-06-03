package org.comroid.restless.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.comroid.restless.HTTPStatusCodes.METHOD_NOT_ALLOWED;
import static org.comroid.restless.HTTPStatusCodes.NOT_FOUND;

public class RestServer {
    private final AutoContextHandler autoContextHandler = new AutoContextHandler();
    private final HttpServer server;
    private final List<ServerEndpoint> endpoints;
    private final REST rest;

    public RestServer(REST rest, InetAddress address, int port, ServerEndpoint... endpoints) throws IOException {
        this.rest = rest;
        this.server = HttpServer.create(new InetSocketAddress(address, port), port);
        this.endpoints = Arrays.asList(endpoints);

        server.createContext("/", autoContextHandler);
        server.setExecutor(rest.getExecutor());
        server.start();
    }

    private class AutoContextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final URI requestURI = exchange.getRequestURI();
            final UniNode node = rest.getSerializationAdapter()
                    .createUniNode(new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                            .lines()
                            .collect(Collectors.joining()));
            Optional<ServerEndpoint> handler = endpoints.stream()
                    .filter(endpoint -> endpoint.test(requestURI))
                    .findFirst();

            if (handler.isPresent()) {
                final ServerEndpoint sep = handler.get();

                if (Arrays.binarySearch(sep.allowedMethods(), REST.Method.valueOf(exchange.getRequestMethod())) == -1) {
                    exchange.sendResponseHeaders(METHOD_NOT_ALLOWED, 0);
                    return;
                }

                final String[] args = sep.extractArgs(requestURI);

                sep.getHandler().handle(node, args);
            } else {
                exchange.sendResponseHeaders(NOT_FOUND, 0);
            }
        }
    }
}
