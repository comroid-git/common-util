package org.comroid.restless.endpoint;

import org.comroid.common.Polyfill;

import java.net.URI;
import java.net.URL;

public interface CompleteEndpoint {
    RestEndpoint getEndpoint();

    String getSpec();

    default URL getURL() {
        return Polyfill.url(getSpec());
    }

    default URI getURI() {
        return Polyfill.uri(getSpec());
    }

    static CompleteEndpoint of(RestEndpoint endpoint, String spec) {
        return new Support.OfSpec(endpoint, spec);
    }

    final class Support {
        private static final class OfSpec implements CompleteEndpoint {
            private final RestEndpoint endpoint;
            private final String spec;

            @Override
            public RestEndpoint getEndpoint() {
                return endpoint;
            }

            @Override
            public String getSpec() {
                return spec;
            }

            private OfSpec(RestEndpoint endpoint, String spec) {
                this.endpoint = endpoint;
                this.spec = spec;
            }
        }
    }
}
