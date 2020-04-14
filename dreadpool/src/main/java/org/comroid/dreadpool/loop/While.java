package org.comroid.dreadpool.loop;

import java.util.function.IntFunction;

import org.comroid.dreadpool.model.Loop;

public abstract class While<T> extends Loop<T> {
    private final IntFunction<T> producer;

    public While(IntFunction<T> producer) {
        super(priority);
        this.producer = producer;
    }

    @Override
    protected final T produce(int loop) {
        return producer.apply(loop);
    }
}
