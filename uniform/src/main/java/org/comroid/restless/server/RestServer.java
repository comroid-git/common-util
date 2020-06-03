package org.comroid.restless.server;

import com.google.common.flogger.FluentLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.comroid.restless.HTTPStatusCodes.METHOD_NOT_ALLOWED;
import static org.comroid.restless.HTTPStatusCodes.NOT_FOUND;

public class RestServer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final AutoContextHandler autoContextHandler = new AutoContextHandler();
    private final HttpServer server;
    private final List<ServerEndpoint> endpoints;
    private final REST rest;
    private final String baseUrl;

    public RestServer(REST rest, String baseUrl, InetAddress address, int port, ServerEndpoint... endpoints) throws IOException {
        logger.at(Level.INFO).log("Starting REST Server with %d endpoints", endpoints.length);
        this.rest = rest;
        this.baseUrl = baseUrl;
        this.server = HttpServer.create(new InetSocketAddress(address, port), port);
        this.endpoints = Arrays.asList(endpoints);

        server.createContext("/", autoContextHandler);
        server.setExecutor(rest.getExecutor());
        server.start();
    }

    private class AutoContextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final String requestURI = baseUrl.substring(0, baseUrl.length() - 1) + exchange.getRequestURI().toString();

            logger.at(Level.INFO).log("Handling %s Request @ %s", exchange.getRequestMethod(), requestURI);

            UniNode node = null;
            try (
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                    BufferedReader br = new BufferedReader(isr)
            ) {
                String data = br.lines().collect(Collectors.joining());

                if (data.isEmpty())
                    node = rest.getSerializationAdapter().createUniObjectNode();
                else node = rest.getSerializationAdapter().createUniNode(data);
            } catch (Throwable t) {
                logger.at(Level.SEVERE).log("Could not deserialize response");
            }

            logger.at(Level.INFO).log("Looking for matching endpoint...");
            Optional<ServerEndpoint> handler = endpoints.stream()
                    .filter(endpoint -> endpoint.test(requestURI))
                    .findFirst();

            if (handler.isPresent()) {
                final ServerEndpoint sep = handler.get();
                logger.at(Level.INFO).log("Endpoint found: %s", sep);

                if (Arrays.binarySearch(sep.allowedMethods(), REST.Method.valueOf(exchange.getRequestMethod())) == -1) {
                    logger.at(Level.INFO).log("Method not allowed for endpoint: %s", exchange.getRequestMethod());
                    exchange.sendResponseHeaders(METHOD_NOT_ALLOWED, 0);
                    return;
                }

                final String[] args = sep.extractArgs(requestURI);
                logger.at(Level.INFO).log("Extracted parameters: %s", Arrays.toString(args));

                logger.at(Level.INFO).log("Executing Handler...");
                final REST.Response response = sep.getHandler().handle(node, args);
                logger.at(Level.INFO).log("Handler Finished! Response: %s", response);

                response.getHeaders().forEach(exchange.getResponseHeaders()::add);

                final String data = response.getBody().toString();

                try (
                    OutputStreamWriter osw = new OutputStreamWriter(exchange.getResponseBody());
                ) {
                    osw.append(data);
                } finally {
                    logger.at(Level.INFO).log("Sending Response...");
                    exchange.sendResponseHeaders(response.getStatusCode(), data.length());
                }
            } else {
                logger.at(Level.INFO).log("Unknown endpoint; returning 404");
                exchange.sendResponseHeaders(NOT_FOUND, 0);
            }

            logger.at(Level.INFO).log("Finished!");
        }
    }
}
