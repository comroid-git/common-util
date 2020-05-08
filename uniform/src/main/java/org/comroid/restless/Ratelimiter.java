package org.comroid.restless;

import org.comroid.common.iter.Span;
import org.comroid.restless.endpoint.RatelimitedEndpoint;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public interface Ratelimiter extends BiFunction<RatelimitedEndpoint, REST.Request, CompletableFuture<REST.Request>> {
    Ratelimiter INSTANT = new Support.Instant();

    static Ratelimiter ofPool(ScheduledExecutorService executor, RatelimitedEndpoint... endpoints) {
        return new Support.OfPool(executor, endpoints);
    }

    /**
     * Applies the provided {@linkplain REST.Request request} with the given {@linkplain RatelimitedEndpoint endpoint}
     * to this ratelimiter and returns a {@linkplain CompletableFuture CompletionStage} that completes with the given
     * {@linkplain REST.Request request} as soon as this ratelimiter allows its execution.
     *
     * @param restEndpoint The endpoint that is gonna be accessed
     * @param request      The request that is yet to be executed
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
            private final Queue<Bucket> upcoming = new LinkedBlockingQueue<>();
            private final ScheduledExecutorService executor;
            private final RatelimitedEndpoint[] pool;
            private final int globalRatelimit;

            private OfPool(ScheduledExecutorService executor, RatelimitedEndpoint[] pool) {
                Span<Integer> globalRatelimits = Stream.of(pool)
                        .map(RatelimitedEndpoint::getGlobalRatelimit)
                        .distinct()
                        .collect(Span.collector());
                if (!globalRatelimits.isSingle())
                    throw new IllegalArgumentException("Global ratelimit is not unique");

                this.executor = executor;
                this.pool = pool;
                this.globalRatelimit = globalRatelimits.requireNonNull();
            }

            @Override
            public CompletableFuture<REST.Request> apply(RatelimitedEndpoint restEndpoint, REST.Request request) {
                if (upcoming.isEmpty() || (restEndpoint.getRatePerSecond() == -1 && restEndpoint.getGlobalRatelimit() == -1))
                    return CompletableFuture.completedFuture(request);

                return null;
            }
        }

        private static final class Bucket {
            private final RatelimitedEndpoint endpoint;
            private final Queue<BoxedRequest> boxedRequests;

            private RatelimitedEndpoint getEndpoint() {
                return endpoint;
            }

            private Queue<BoxedRequest> getQueue() {
                return boxedRequests;
            }

            private Bucket(RatelimitedEndpoint endpoint, REST.Request... requests) {
                this.endpoint = endpoint;
                this.boxedRequests = Arrays.stream(requests)
                        .map(BoxedRequest::new)
                        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
            }

            private BoxedRequest addRequest(REST.Request request) {
                final BoxedRequest boxed = new BoxedRequest(request);

                boxedRequests.add(boxed);
                return boxed;
            }
        }

        private static final class BoxedRequest {
            private final CompletableFuture<REST.Request> future = new CompletableFuture<>();
            private final REST.Request request;

            private BoxedRequest(REST.Request request) {
                this.request = request;
            }

            private void complete() {
                future.complete(request);
            }
        }
    }
}
