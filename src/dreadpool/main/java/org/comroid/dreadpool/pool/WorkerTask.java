package org.comroid.dreadpool.pool;

import java.util.Comparator;

final class WorkerTask implements Runnable {
    public static final Comparator<WorkerTask> COMPARATOR = Comparator.comparingLong(WorkerTask::getExecutionTime);
    private final long execution;
    private final Runnable task;

    public long getExecutionTime() {
        return execution;
    }

    WorkerTask(long execution, Runnable task) {
        this.execution = execution;
        this.task = task;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public String toString() {
        return String.format("WorkerTask{<%s>@%d}", task.toString(), execution);
    }
}
