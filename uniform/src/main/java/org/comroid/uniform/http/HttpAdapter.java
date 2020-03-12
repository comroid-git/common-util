package org.comroid.uniform.http;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface HttpAdapter {
    CompletableFuture<REST.Response> call(REST.Method method, URL url, Collection<REST.Header> headers, String mimeType, String body);
}
