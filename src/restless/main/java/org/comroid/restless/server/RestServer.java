package org.comroid.restless.server;

import com.google.common.flogger.FluentLogger;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.comroid.restless.CommonHeaderNames;
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

import static com.google.common.flogger.LazyArgs.lazy;
import static org.comroid.restless.HTTPStatusCodes.*;

public class RestServer {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final AutoContextHandler autoContextHandler = new AutoContextHandler();
    private final HttpServer server;
    private final List<ServerEndpoint> endpoints;
    private final REST rest;
    private final String mimeType;
    private final String baseUrl;
    private final REST.Header.List commonHeaders = new REST.Header.List();

    public RestServer(REST rest, String baseUrl, InetAddress address, int port, ServerEndpoint... endpoints) throws IOException {
        logger.at(Level.INFO).log("Starting REST Server with %d endpoints", endpoints.length);
        this.rest = rest;
        this.mimeType = rest.getSerializationAdapter().getMimeType();
        this.baseUrl = baseUrl;
        this.server = HttpServer.create(new InetSocketAddress(address, port), port);
        this.endpoints = Arrays.asList(endpoints);

        server.createContext("/", autoContextHandler);
        server.setExecutor(rest.getExecutor());
        server.start();
    }

    public RestServer addCommonHeader(String name, String value) {
        this.commonHeaders.add(name, value);
        return this;
    }

    public boolean removeCommonHeader(String name) {
        return this.commonHeaders.removeIf(header -> header.getName().equals(name));
    }

    private class AutoContextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final String requestURI = baseUrl.substring(0, baseUrl.length() - 1) + exchange.getRequestURI().toString();

            final Headers responseHeaders = exchange.getResponseHeaders();
            commonHeaders.forEach(responseHeaders::add);
            responseHeaders.add(CommonHeaderNames.ACCEPTED_CONTENT_TYPE, mimeType);
            responseHeaders.add(CommonHeaderNames.REQUEST_CONTENT_TYPE, mimeType);

            if (commonHeaders.stream().noneMatch(header -> header.getName().equals("Cookie")))
                responseHeaders.add("Cookie", exchange.getRequestHeaders().getFirst("Cookie"));

            logger.at(Level.INFO).log("Handling %s Request @ %s with Headers: %s", exchange.getRequestMethod(), requestURI,
                    lazy(() -> exchange.getRequestHeaders()
                            .entrySet()
                            .stream()
                            .map(entry -> String.format("%s: %s", entry.getKey(), Arrays.toString(entry.getValue().toArray())))
                            .collect(Collectors.joining("\n- ", "\n- ", ""))
                    )
            );

            final String mimeType = rest.getSerializationAdapter().getMimeType();
            final List<String> targetMimes = exchange.getRequestHeaders().get("Accept");
            if (!supportedMimeType(targetMimes)) {
                logger.at(Level.INFO).log(
                        "Content Type %s not supported, cancelling. Accept Header: %s",
                        mimeType,
                        targetMimes
                );
                exchange.sendResponseHeaders(UNSUPPORTED_MEDIA_TYPE, 0);
                return;
            }

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

                response.getHeaders().forEach(responseHeaders::add);

                final String data = response.getBody().toString();

                try (
                        OutputStreamWriter osw = new OutputStreamWriter(exchange.getResponseBody())
                ) {
                    osw.append(data);
                } finally {
                    logger.at(Level.INFO).log("Sending Response code %d with length %d and Headers: %s",
                            response.getStatusCode(),
                            data.length(),
                            lazy(() -> responseHeaders
                                    .entrySet()
                                    .stream()
                                    .map(entry -> String.format("%s: %s", entry.getKey(), Arrays.toString(entry.getValue().toArray())))
                                    .collect(Collectors.joining("\n- ", "\n- ", ""))
                            )
                    );
                    exchange.sendResponseHeaders(response.getStatusCode(), data.length());
                }
            } else {
                logger.at(Level.INFO).log("Unknown endpoint; returning 404");
                exchange.sendResponseHeaders(NOT_FOUND, 0);
            }

            logger.at(Level.INFO).log("Finished!");
        }

        private boolean supportedMimeType(List<String> targetMimes) {
            return targetMimes.stream().anyMatch(type -> type.contains(mimeType) || type.contains("*/*"));
        }
    }
}
