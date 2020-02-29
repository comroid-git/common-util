package org.comroid.common.rest.adapter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.rest.REST;
import org.comroid.common.rest.uniform.Adapter;
import org.comroid.common.rest.uniform.SerializerAdapter;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttp3Adapter<SER extends SerializerAdapter<DAT, TYP>, DAT, TYP> extends Adapter.Abstract<OkHttpClient, SER, DAT, TYP> {
    private final MediaType mediaType;

    public OkHttp3Adapter(SER serializer, MediaType mediaType) {
        this(new OkHttpClient.Builder().build(), serializer, mediaType);
    }

    public OkHttp3Adapter(OkHttpClient httpClient, SER serializer, MediaType mediaType) {
        super(httpClient, serializer);
        this.mediaType = mediaType;
    }

    @Override
    public CompletableFuture<REST.Response> call(REST.Request.Builder<?, ?> requestBuilder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final REST.Request.Action action = requestBuilder.getAction();
                final Request.Builder builder = new Request.Builder().url(requestBuilder.getUrl());

                if (action.getMethod() != REST.Method.GET)
                    builder.method(action.getMethod().toString(), RequestBody.create(mediaType, action.getBody().toString()));
                else builder.get();
                requestBuilder.getHeaders().forEach(head -> builder.header(head.getName(), head.getValue()));

                final Request request = builder.build();
                final Call call = httpClient.newCall(request);
                final Response response = call.execute();

                return new REST.Response(
                        REST.Request.Body.of(Objects.requireNonNull(response.body(), "response body").string()),
                        response.code()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
