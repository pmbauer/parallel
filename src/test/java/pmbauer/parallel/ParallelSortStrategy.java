package pmbauer.parallel;

import java.util.concurrent.ExecutorService;

abstract class ParallelSortStrategy implements AutoCloseable {
    private final String description;
    ExecutorService pool;

    ParallelSortStrategy(String algorithmName, int numberOfThreads) {
        description = algorithmName + ", Threads=" + numberOfThreads;
    }

    public abstract void sort(int[] a);

    public String getDescription() {
        return description;
    }

    @Override
    public void close() throws Exception {
        if (pool != null)
            pool.shutdown();
    }
}