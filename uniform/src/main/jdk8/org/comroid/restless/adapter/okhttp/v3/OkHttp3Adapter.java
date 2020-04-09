package org.comroid.restless.adapter.okhttp.v3;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.func.Provider;
import org.comroid.restless.HttpAdapter;
import org.comroid.restless.REST;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttp3Adapter implements HttpAdapter {
    private final OkHttpClient httpClient = new OkHttpClient.Builder().build();

    @Override
    public CompletableFuture<REST.Response> call(
            REST rest,
            REST.Method method,
            Provider<URL> urlProvider,
            Collection<REST.Header> headers,
            String mimeType,
            String body
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final Request.Builder builder = new Request.Builder().url(urlProvider.now())
                        // only support null body for GET method, else throw
                        .method(
                                method.toString(), (
                                        body == null && method == REST.Method.GET
                                                ? null
                                                : RequestBody.create(
                                                MediaType.parse(mimeType),
                                                Objects.requireNonNull(body, "Null body not supported with " + method)
                                        ))
                        );

                headers.forEach(header -> builder.addHeader(header.getName(), header.getValue()));

                final Request      request      = builder.build();
                final Call         call         = httpClient.newCall(request);
                final Response     response     = call.execute();
                final ResponseBody responseBody = response.body();

                return new REST.Response(
                        rest, response.code(), responseBody == null ? null : responseBody.string());
            } catch (IOException e) {
                throw new RuntimeException("Request failed", e);
            }
        });
    }
}
