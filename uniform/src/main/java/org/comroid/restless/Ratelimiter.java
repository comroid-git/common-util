package org.comroid.restless;

import org.comroid.common.iter.Span;
import org.comroid.restless.endpoint.RatelimitedEndpoint;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public interface Ratelimiter extends BiFunction<RatelimitedEndpoint, REST.Request, CompletableFuture<REST.Request>> {
    Ratelimiter INSTANT = new Support.Instant();

    static Ratelimiter ofEndpoints(RatelimitedEndpoint... endpoints) {
        return new Support.OfPool(endpoints);
    }

    /**
     * Applies the provided {@linkplain REST.Request request} with the given {@linkplain RatelimitedEndpoint endpoint}
     * to this ratelimiter and returns a {@linkplain CompletableFuture CompletionStage} that completes with the given
     * {@linkplain REST.Request request} as soon as this ratelimiter allows its execution.
     *
     * @param restEndpoint The endpoint that is gonna be accessed
     * @param request The request that is yet to be executed
     * @return A CompletableFuture that completes as soon as the ratelimiter allows execution of the request.
     */
    @Override
    CompletableFuture<REST.Request> apply(RatelimitedEndpoint restEndpoint, REST.Request request);

    final class Support {
        private static final class Instant implements Ratelimiter {
            private Instant() {
            }

            @Override
            public CompletableFuture<REST.Request> apply(RatelimitedEndpoint restEndpoint, REST.Request request) {
                return CompletableFuture.completedFuture(request);
            }
        }

        private static final class OfPool implements Ratelimiter {
            private final RatelimitedEndpoint[] pool;
            private final int globalRatelimit;

            private OfPool(RatelimitedEndpoint[] pool) {
                Span<Integer> globalRatelimits = Stream.of(pool)
                        .map(RatelimitedEndpoint::getGlobalRatelimit)
                        .distinct()
                        .collect(Span.collector());
                if (!globalRatelimits.isSingle())
                    throw new IllegalArgumentException("Global ratelimit is not unique");

                this.pool = pool;
                this.globalRatelimit = globalRatelimits.requireNonNull();
            }

            @Override
            public CompletableFuture<REST.Request> apply(RatelimitedEndpoint restEndpoint, REST.Request request) {
                if (restEndpoint.getRatePerSecond() == -1 && restEndpoint.getGlobalRatelimit() == -1)
                    return CompletableFuture.completedFuture(request);

                return null;
            }
        }
    }
}
