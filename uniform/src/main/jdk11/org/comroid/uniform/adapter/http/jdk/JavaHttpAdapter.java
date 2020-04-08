package org.comroid.uniform.adapter.http.jdk;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.comroid.uniform.http.HttpAdapter;
import org.comroid.uniform.http.REST;

public final class JavaHttpAdapter implements HttpAdapter {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<REST.Response> call(
            REST rest,
            REST.Method method,
            URL url,
            Collection<REST.Header> headers,
            String mimeType,
            String body
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpRequest.Builder builder = HttpRequest.newBuilder(url.toURI())
                        .method(
                                method.toString(),
                                HttpRequest.BodyPublishers.ofString(
                                        body)
                        )
                        .header("Content-Type", mimeType);

                headers.forEach(header -> builder.header(header.getName(), header.getValue()));

                final HttpRequest request = builder.build();
                final HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString());

                return new REST.Response(rest, response.statusCode(), response.body());
            } catch (Throwable e) {
                throw new RuntimeException("Request failed", e);
            }
        });
    }
}