package org.comroid.common.rest.adapter.http;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.rest.REST;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpAdapter$OkHttp3 implements HttpAdapter {
    private final OkHttpClient httpClient = new OkHttpClient.Builder().build();

    @Override
    public CompletableFuture<REST.Response> call(REST.Method method, URL url, Collection<REST.Header> headers, String mimeType, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final Request.Builder builder = new Request.Builder()
                        .url(url)
                        .method(method.toString(), RequestBody.create(MediaType.parse(mimeType), body));

                headers.forEach(header -> builder.addHeader(header.getName(), header.getValue()));

                final Request request = builder.build();
                final Call call = httpClient.newCall(request);
                final Response response = call.execute();
                final ResponseBody responseBody = response.body();

                return new REST.Response(response.code(), responseBody == null ? null : responseBody.string());
            } catch (IOException e) {
                throw new RuntimeException("Request failed", e);
            }
        });
    }
}
