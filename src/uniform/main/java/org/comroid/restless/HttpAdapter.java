package org.comroid.restless;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.comroid.common.func.Provider;
import org.comroid.uniform.SerializationAdapter;

@SuppressWarnings("rawtypes")
public interface HttpAdapter {
    static HttpAdapter autodetect() {
        throw new UnsupportedOperationException();
    }

    CompletableFuture<REST.Response> call(REST.Request request, String mimeType);
}
