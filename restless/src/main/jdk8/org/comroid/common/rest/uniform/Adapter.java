package org.comroid.common.rest.uniform;

import java.util.concurrent.CompletableFuture;

import org.comroid.common.rest.REST;

public interface Adapter<CLI, SER extends SerializerAdapter<DAT, TYP>, DAT, TYP> {
    CLI getHttpClient();

    SER getSerializer();

    CompletableFuture<REST.Response> call(REST.Request.Builder<?, ?> requestBuilder);
    
    abstract class Abstract<CLI, SER extends SerializerAdapter<DAT, TYP>, DAT, TYP> implements Adapter<CLI, SER, DAT, TYP> {
        protected final CLI httpClient;
        protected final SER serializer;

        protected Abstract(CLI httpClient, SER serializer) {
            this.httpClient = httpClient;
            this.serializer = serializer;
        }

        @Override 
        public CLI getHttpClient() {
            return httpClient;
        }

        @Override
        public SER getSerializer() {
            return serializer;
        }
    }
}
