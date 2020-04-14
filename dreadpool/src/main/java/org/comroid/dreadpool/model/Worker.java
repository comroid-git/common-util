package org.comroid.dreadpool.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Worker extends Thread {
    protected Worker(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    @Override
    public abstract void run();
}
