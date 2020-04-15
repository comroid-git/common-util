package org.comroid.dreadpool.loop.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Worker extends Thread {
    protected Worker(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    @Override
    public abstract void run();
}
