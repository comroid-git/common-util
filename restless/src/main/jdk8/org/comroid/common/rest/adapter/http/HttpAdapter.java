package org.comroid.common.rest.adapter.http;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.comroid.common.rest.REST;

public interface HttpAdapter {
    CompletableFuture<REST.Response> call(REST.Method method, URL url, Collection<REST.Header> headers, String mimeType, String body);
}
