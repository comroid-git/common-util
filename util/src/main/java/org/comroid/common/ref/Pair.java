package org.comroid.common.ref;

public final class Pair<A, B> {
    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first  = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("Pair{first=%s, second=%s}", first, second);
    }
}
