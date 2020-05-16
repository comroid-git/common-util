package org.comroid.common.ref;

public class Pair<A, B> {
    protected final Reference.Settable<A> first;
    protected final Reference.Settable<B> second;

    public A getFirst() {
        return first.get();
    }

    public B getSecond() {
        return second.get();
    }

    public Pair(A first, B second) {
        this.first = Reference.Settable.create(first);
        this.second = Reference.Settable.create(second);
    }

    @Override
    public String toString() {
        return String.format("Pair{first=%s, second=%s}", getFirst(), getSecond());
    }
}
