package org.comroid.common.rest.jdk11;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.rest.REST;
import org.comroid.common.rest.uniform.Adapter;
import org.comroid.common.rest.uniform.SerializerAdapter;

public final class Jdk11Adapter<SER extends SerializerAdapter<DAT, TYP>, DAT, TYP> extends Adapter.Abstract<HttpClient, SER, DAT, TYP> {
    public Jdk11Adapter(SER serializer) {
        this(HttpClient.newHttpClient(), serializer);
    }

    public Jdk11Adapter(HttpClient httpClient, SER serializer) {
        super(httpClient, serializer);
    }

    @Override
    public CompletableFuture<REST.Response> call(REST.Request.Builder<?, ?> requestBuilder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final REST.Request.Action action = requestBuilder.getAction();
                final HttpRequest.Builder builder = HttpRequest.newBuilder(requestBuilder.getUrl().toURI());

                if (action.getMethod() != REST.Method.GET)
                    builder.method(action.getMethod().toString(), HttpRequest.BodyPublishers.ofString(action.getBody().toString()));
                else builder.GET();
                requestBuilder.getHeaders().forEach(head -> builder.header(head.getName(), head.getValue()));

                final HttpRequest request = builder.build();
                final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return new REST.Response(
                        REST.Request.Body.of(response.body()),
                        response.statusCode()
                );
            } catch (InterruptedException | IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
