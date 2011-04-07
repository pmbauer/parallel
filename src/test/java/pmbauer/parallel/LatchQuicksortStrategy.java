package pmbauer.parallel;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class LatchQuicksortStrategy extends ParallelSortStrategy {

    public LatchQuicksortStrategy(int numberOfThreads) {
        super("Latch Quicksort", numberOfThreads);
        pool = new ThreadPoolExecutor(numberOfThreads,
                                      numberOfThreads,
                                      Long.MAX_VALUE,
                                      TimeUnit.SECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void sort(int[] a) {
        try {
            Quicksort.latchQuicksort(pool, a);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}