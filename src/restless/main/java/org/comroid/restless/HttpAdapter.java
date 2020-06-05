package org.comroid.restless;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("rawtypes")
public interface HttpAdapter {
    static HttpAdapter autodetect() {
        throw new UnsupportedOperationException();
    }

    CompletableFuture<REST.Response> call(REST.Request request, String mimeType);
}
