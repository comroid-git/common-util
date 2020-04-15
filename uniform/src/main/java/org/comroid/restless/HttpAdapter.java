package org.comroid.restless;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.func.Provider;

public interface HttpAdapter {
    CompletableFuture<REST.Response> call(
            REST rest, REST.Method method,
            Provider<URL> urlProvider,
            Collection<REST.Header> headers,
            String mimeType,
            String body
    );
}