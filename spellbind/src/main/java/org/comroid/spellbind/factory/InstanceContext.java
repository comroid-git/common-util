package org.comroid.spellbind.factory;

import java.util.Arrays;

import org.comroid.common.ref.SelfDeclared;

public abstract class InstanceContext<S extends InstanceContext<S>> implements SelfDeclared<S> {
    private final Object[] args;

    protected InstanceContext(Object... args) {
        this.args = args;
    }

    public final Object[] getArgs() {
        return args;
    }

    @Override
    public final String toString() {
        return String.format("InstanceContext{args=%s}", Arrays.toString(args));
    }
}
