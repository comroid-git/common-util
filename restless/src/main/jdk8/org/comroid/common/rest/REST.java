package org.comroid.common.rest;

import java.util.Objects;

import org.comroid.common.rest.adapter.data.DataAdapter;
import org.comroid.common.rest.adapter.http.HttpAdapter;

public final class REST<HA extends HttpAdapter, DA extends DataAdapter> {
    public static <HA extends HttpAdapter, DA extends DataAdapter> REST<HA, DA> newClient(
            HA httpAdapter,
            DA dataAdapter
    ) {
        return new REST<>(httpAdapter, dataAdapter);
    }

    private final HA httpAdapter;
    private final DA dataAdapter;

    private REST(HA httpAdapter, DA dataAdapter) {
        this.httpAdapter = Objects.requireNonNull(httpAdapter, "HttpAdapter");
        this.dataAdapter = Objects.requireNonNull(dataAdapter, "DataAdapter");
    }

    public static final class Request<T> {
    }
}
