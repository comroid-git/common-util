package org.comroid.common.rest.uniform;

import java.util.concurrent.CompletableFuture;

import org.comroid.common.rest.REST;

public interface Adapter<CLI, SER extends SerializerAdapter<DAT, TYP>, DAT, TYP> {
    CLI getHttpClient();

    SER getSerializer();

    CompletableFuture<REST.Response> call(REST.Request.Builder<?, ?> builder);
}
