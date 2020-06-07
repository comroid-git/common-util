package org.comroid.restless.server;

import com.google.common.flogger.FluentLogger;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.comroid.restless.CommonHeaderNames;
import org.comroid.restless.REST;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        public void handle(HttpExchange exchange) {
            final String requestURI = baseUrl.substring(0, baseUrl.length() - 1) + exchange.getRequestURI().toString();
            final REST.Method requestMethod = REST.Method.valueOf(exchange.getRequestMethod());
            final String requestString = String.format("%s @ %s", requestMethod, requestURI);

            try {
                final Headers responseHeaders = exchange.getResponseHeaders();
                final Headers requestHeaders = exchange.getRequestHeaders();
                commonHeaders.forEach(responseHeaders::add);
                responseHeaders.add(CommonHeaderNames.ACCEPTED_CONTENT_TYPE, mimeType);
                responseHeaders.add(CommonHeaderNames.REQUEST_CONTENT_TYPE, mimeType);

                if (commonHeaders.stream().noneMatch(header -> header.getName().equals("Cookie"))
                        && requestHeaders.containsKey("Cookie"))
                    responseHeaders.add("Cookie", requestHeaders.getFirst("Cookie"));

                logger.at(Level.INFO).log("Handling %s Request @ %s with Headers: %s", requestMethod, requestURI,
                        lazy(() -> requestHeaders
                                .entrySet()
                                .stream()
                                .map(entry -> String.format("%s: %s", entry.getKey(), Arrays.toString(entry.getValue().toArray())))
                                .collect(Collectors.joining("\n- ", "\n- ", ""))
                        )
                );

                final String mimeType = rest.getSerializationAdapter().getMimeType();
                final List<String> targetMimes = requestHeaders.get("Accept");
                if (!supportedMimeType(targetMimes)) {
                    logger.at(Level.INFO).log(
                            "Content Type %s not supported, cancelling. Accept Header: %s",
                            mimeType,
                            targetMimes
                    );
                    writeResponse(exchange, UNSUPPORTED_MEDIA_TYPE);
                    return;
                }

                UniNode node = consumeBody(exchange);

                logger.at(Level.INFO).log("Looking for matching endpoint...");
                Optional<ServerEndpoint> handler = endpoints.stream()
                        .filter(endpoint -> endpoint.test(requestURI))
                        .findFirst();

                if (handler.isPresent()) {
                    final ServerEndpoint sep = handler.get();
                    logger.at(Level.INFO).log("Endpoint found: %s", sep);

                    final String[] args = sep.extractArgs(requestURI);
                    logger.at(Level.INFO).log("Extracted parameters: %s", Arrays.toString(args));

                    logger.at(Level.INFO).log("Executing Handler for method: %s", requestMethod);
                    REST.Response response;
                    try {
                        response = sep.executeMethod(requestMethod, requestHeaders, args, node);
                    } catch (RestEndpointException reex) {
                        logger.at(Level.INFO).log("Handler threw exception: " + reex.getMessage());

                        final String rsp = generateErrorNode(reex).toString();
                        writeResponse(exchange, reex.getStatusCode(), rsp);

                        return;
                    }

                    logger.at(Level.INFO).log("Handler Finished! Response: %s", response);

                    if (response == null) {
                        writeResponse(exchange, OK);
                        return;
                    }

                    response.getHeaders().forEach(responseHeaders::add);

                    final String data = response.getBody().toString();
                    writeResponse(exchange, response.getStatusCode(), data);

                    logger.at(Level.INFO).log("Sent Response code %d with length %d and Headers: %s",
                            response.getStatusCode(),
                            data.length(),
                            lazy(() -> responseHeaders
                                    .entrySet()
                                    .stream()
                                    .map(entry -> String.format("%s: %s", entry.getKey(), Arrays.toString(entry.getValue().toArray())))
                                    .collect(Collectors.joining("\n- ", "\n- ", ""))
                            )
                    );
                } else {
                    logger.at(Level.INFO).log("Unknown endpoint; returning 404");
                    writeResponse(exchange, NOT_FOUND);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                exchange.close();
                logger.at(Level.INFO).log("Finished handling %s", requestString);
            }
        }

        private void writeResponse(HttpExchange exchange, int statusCode) throws IOException {
            writeResponse(exchange, statusCode, rest.getSerializationAdapter()
                    .createUniObjectNode()
                    .toString());
        }

        private void writeResponse(HttpExchange exchange, int statusCode, String data) throws IOException {
            exchange.sendResponseHeaders(statusCode, data.length());
            final OutputStream osr = exchange.getResponseBody();
            osr.write(data.getBytes());
            osr.flush();
        }

        private UniObjectNode generateErrorNode(RestEndpointException reex) {
            final UniObjectNode rsp = rest.getSerializationAdapter().createUniObjectNode();

            rsp.put("code", ValueType.INTEGER, reex.getStatusCode());
            rsp.put("message", ValueType.STRING, reex.getSimpleMessage());

            return rsp;
        }

        private UniNode consumeBody(HttpExchange exchange) {
            try (
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                    BufferedReader br = new BufferedReader(isr)
            ) {
                String data = br.lines().collect(Collectors.joining());

                if (data.isEmpty())
                    return rest.getSerializationAdapter().createUniObjectNode();
                else return rest.getSerializationAdapter().createUniNode(data);
            } catch (Throwable t) {
                logger.at(Level.SEVERE).log("Could not deserialize response");
            }

            return null;
        }

        private boolean supportedMimeType(List<String> targetMimes) {
            return targetMimes.stream().anyMatch(type -> type.contains(mimeType) || type.contains("*/*"));
        }
    }
}
