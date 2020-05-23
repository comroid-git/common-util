package org.comroid.restless.adapter.jdk;

import org.comroid.restless.CommonHeaderNames;
import org.comroid.restless.HttpAdapter;
import org.comroid.restless.REST;
import org.comroid.uniform.node.UniNode;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public final class JavaHttpAdapter implements HttpAdapter {
    private final HttpClient httpClient;

    public JavaHttpAdapter() {
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public CompletableFuture<REST.Response> call(REST.Request request, String mimeType) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder(request.getEndpoint().getURI())
                .header(CommonHeaderNames.REQUEST_CONTENT_TYPE, mimeType);

        request.getHeaders().forEach(builder::header);
        final HttpRequest.BodyPublisher publisher = request.getMethod() == REST.Method.GET
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(request.getBody(), StandardCharsets.UTF_8);

        return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    final String body = response.body();

                    if (body == null)
                        return new REST.Response(response.statusCode(), null);

                    final UniNode data = request.getREST()
                            .getSerializationAdapter()
                            .createUniNode(body);

                    return new REST.Response(response.statusCode(), data);
                });
    }
}
