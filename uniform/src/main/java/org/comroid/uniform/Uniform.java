package org.comroid.uniform;

import java.util.Objects;

import org.comroid.common.util.ReflectionHelper;
import org.comroid.uniform.data.SeriLib;
import org.comroid.uniform.http.HttpAdapter;

public final class Uniform {
    private static Configuration autoConfig;

    public static Configuration autoConfiguration() {
        HttpAdapter httpAdapter = null;
        SeriLib<?, ?, ?> seriLib = null;

        try {
            if (!isJRE11()) {
                // find httpAdapter
            } else httpAdapter = ReflectionHelper.instance((Class<? extends HttpAdapter>) Class.forName("org.comroid.uniform.http.impl.Java11HttpAdapter"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find class", e);
        }

        return autoConfig == null
                ? (autoConfig = new Configuration(
                Objects.requireNonNull(httpAdapter, "Could not determine HttpAdapter automatically"),
                Objects.requireNonNull(seriLib, "Could not determine serialization library automatically")))
                : autoConfig;
    }

    private static boolean isJRE11() {
        return Double.parseDouble(System.getProperty("java.version")) >= 11;
    }

    public static final class Configuration {
        private final HttpAdapter httpAdapter;
        private final SeriLib<?, ?, ?> serializationLibrary;

        public Configuration(HttpAdapter httpAdapter, SeriLib<?, ?, ?> serializationLibrary) {
            this.httpAdapter = httpAdapter;
            this.serializationLibrary = serializationLibrary;
        }

        public HttpAdapter getHttpAdapter() {
            return httpAdapter;
        }

        public SeriLib<?, ?, ?> getSerializationLibrary() {
            return serializationLibrary;
        }
    }
}
