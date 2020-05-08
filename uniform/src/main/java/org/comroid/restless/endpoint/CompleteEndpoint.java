package org.comroid.restless.endpoint;

import org.comroid.common.Polyfill;

import java.net.URI;
import java.net.URL;
import java.util.function.Function;

public interface CompleteEndpoint {
    static CompleteEndpoint of(RestEndpoint endpoint, Object... args) {
        return new Support.OfArgs(endpoint, args);
    }

    RestEndpoint getEndpoint();

    String getSpec();

    default URL getURL() {
        return Polyfill.url(getSpec());
    }

    default URI getURI() {
        return Polyfill.uri(getSpec());
    }

    final class Support {
        private static final class OfArgs implements CompleteEndpoint {
            private final RestEndpoint endpoint;
            private final String spec;

            private OfArgs(RestEndpoint endpoint, Object[] args) {
                this.endpoint = endpoint;
                this.spec = endpoint.makeAndValidateUrl(Function.identity(), args);
            }

            @Override
            public RestEndpoint getEndpoint() {
                return endpoint;
            }

            @Override
            public String getSpec() {
                return spec;
            }
        }
    }
}
