package org.comroid.varbind.bind;

import java.util.function.Supplier;

public abstract class VarBind<T> extends Supplier<T>, GroupedBind {
    public T def() {
    }

    public T cast(Object inst) {
    }
}
