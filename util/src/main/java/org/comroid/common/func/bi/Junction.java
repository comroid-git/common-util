package org.comroid.common.func.bi;

import java.util.function.Function;

public interface Junction<A, B> {
    B forward(A a);

    A backward(B b);

    static <A, B> Junction<A, B> of(Function<A, B> pForward, Function<B, A> pBackward) {
        return new Junction<A, B>() {
            private final Function<A, B> forward = pForward;
            private final Function<B, A> backward = pBackward;

            @Override
            public B forward(A a) {
                return forward.apply(a);
            }

            @Override
            public A backward(B b) {
                return backward.apply(b);
            }
        };
    }
}
