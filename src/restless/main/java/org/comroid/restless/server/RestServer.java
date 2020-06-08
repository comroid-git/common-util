package org.comroid.restless.server;

import com.google.common.flogger.FluentLogger;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.comroid.mutatio.span.Span;
import org.comroid.restless.CommonHeaderNames;
import org.comroid.restless.HTTPStatusCodes;
import org.comroid.restless.REST;
import org.comroid.restless.REST.Response;
import org.comroid.uniform.ValueType;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.google.common.flogger.LazyArgs.lazy;
import static org.comroid.restless.HTTPStatusCodes.*;

public class RestServer implements Closeable {
    private static final Response dummyResponse = new Response(0, null);
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final AutoContextHandler autoContextHandler = new AutoContextHandler();
    private final HttpServer server;
    private final Span<ServerEndpoint> endpoints;
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
        this.endpoints = Span.immutable(endpoints);

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

    @Override
    public void close() {
        server.stop(5);
    }

    private class AutoContextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            final String requestURI = baseUrl.substring(0, baseUrl.length() - 1) + exchange.getRequestURI().toString();
            final REST.Method requestMethod = REST.Method.valueOf(exchange.getRequestMethod());
            final String requestString = String.format("%s @ %s", requestMethod, requestURI);

            try {
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

                        throw new RestEndpointException(UNSUPPORTED_MEDIA_TYPE, String.format(
                                "Content Type %s not supported, cancelling. Accept Header: %s",
                                mimeType,
                                targetMimes
                        ));
                    }

                    UniNode node = consumeBody(exchange);

                    logger.at(Level.INFO).log("Looking for matching endpoint...");
                    forwardToEndpoint(exchange, requestURI, requestMethod, responseHeaders, requestHeaders, node);
                } catch (Throwable t) {
                    throw new RestEndpointException(INTERNAL_SERVER_ERROR, t);
                }
            } catch (RestEndpointException reex) {
                logger.at(Level.SEVERE)
                        .withCause(reex)
                        .log("An exception occurred during handling: " + reex.getMessage());

                final String rsp = generateErrorNode(reex).toString();
                try {
                    writeResponse(exchange, reex.getStatusCode(), rsp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                exchange.close();
                logger.at(Level.INFO).log("Finished handling %s", requestString);
            }
        }

        private void forwardToEndpoint(
                HttpExchange exchange,
                String requestURI,
                REST.Method requestMethod,
                Headers responseHeaders,
                Headers requestHeaders,
                UniNode requestBody) throws RestEndpointException, IOException {
            final Iterator<ServerEndpoint> iter = endpoints.pipe()
                    // endpoints that accept the request uri
                    .filter(endpoint -> endpoint.test(requestURI))
                    // handle member accessing endpoints with lower priority
                    .sorted(Comparator.comparingInt(endpoint -> endpoint.isMemberAccess(requestURI) ? 1 : -1))
                    .span()
                    .iterator();
            RestEndpointException lastException = null;
            Response response = dummyResponse;

            if (!iter.hasNext()) {
                logger.at(Level.INFO).log("No endpoints found; returning 404");

                throw new RestEndpointException(NOT_FOUND, "No endpoint found at URL: " + requestURI);
            }

            while (iter.hasNext()) {
                final ServerEndpoint endpoint = iter.next();

                if (endpoint.supports(requestMethod)) {
                    final String[] args = endpoint.extractArgs(requestURI);
                    logger.at(Level.INFO).log("Extracted parameters: %s", Arrays.toString(args));

                    if (args.length != endpoint.getParameterCount() && !endpoint.allowMemberAccess())
                        throw new RestEndpointException(BAD_REQUEST, "Invalid argument Count");

                    try {
                        logger.at(Level.INFO).log("Executing Handler for method: %s", requestMethod);
                        response = endpoint.executeMethod(requestMethod, requestHeaders, args, requestBody);
                    } catch (RestEndpointException reex) {
                        lastException = reex;
                    }

                    if (response == dummyResponse) {
                        logger.at(Level.INFO).log("Handler could not complete normally, attempting next handler...", response);
                        continue;
                    }

                    logger.at(Level.INFO).log("Handler Finished! Response: %s", response);
                    handleResponse(exchange, requestURI, endpoint, responseHeaders, response);
                    lastException = null;
                    break;
                }
            }

            if (lastException != null)
                throw lastException;
        }

        private void handleResponse(HttpExchange exchange, String requestURI, ServerEndpoint sep, Headers responseHeaders, REST.Response response) throws IOException {
            if (response == null) {
                writeResponse(exchange, OK);
                return;
            }

            response.getHeaders().forEach(responseHeaders::add);
            final UniNode responseBody = response.getBody();
            final String data = unwrapData(sep, requestURI, responseBody);

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
        }

        private String unwrapData(ServerEndpoint sep, String requestURI, UniNode responseBody) {
            if (responseBody == null)
                return "";
            if (!sep.allowMemberAccess() || !sep.isMemberAccess(requestURI))
                return responseBody.toString();

            String fractalName = requestURI.substring(requestURI.lastIndexOf("/") + 1);

            if (fractalName.matches("\\d+")) {
                // numeric fractal
                final int fractalNum = Integer.parseInt(fractalName);

                if (!responseBody.has(fractalNum))
                    fractalName = null;

                if (fractalName != null)
                    return responseBody.get(fractalNum).toString();
            } else {
                // string fractal
                if (!responseBody.has(fractalName))
                    fractalName = null;

                if (fractalName != null)
                    return responseBody.get(fractalName).toString();
            }

            return responseBody.toString();
        }

        private void writeResponse(HttpExchange exchange, int statusCode) throws IOException {
            writeResponse(exchange, statusCode, "");
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
            rsp.put("description", ValueType.STRING, HTTPStatusCodes.toString(reex.getStatusCode()));
            rsp.put("message", ValueType.STRING, reex.getSimpleMessage());

            final Throwable cause = reex.getCause();
            if (cause != null)
                rsp.put("cause", ValueType.STRING, cause.toString());

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
