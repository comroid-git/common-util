package org.comroid.varbind.bind;

import java.util.function.Supplier;

public abstract class VarBind<T> implements Supplier<T>, GroupedBind {
    public T def() {
        return null; // todo
    }

    public T cast(Object inst) {
        return null; // todo
    }
}
