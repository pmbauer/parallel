package pmbauer.parallel;

import pmbauer.concurrent.CountDownLatch;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static pmbauer.parallel.Quicksort.partition;

/**
 * Root task to encapsulate:
 * <ul>
 *     <li>array to sort</li>
 *     <li>sorting threshold : computed in the constructor; sub-tasks smaller than this are serially sorted</li>
 *     <li>latch : synchronization object that signals when the work is complete</li>
 *     <li>sub-task scheduling</li>
 * </ul>
 */
public class LatchQuicksortTask implements Runnable {
    private final int SERIAL_THRESHOLD;
    private final ExecutorService pool;
    private final CountDownLatch latch;
    private final int[] a;

    public LatchQuicksortTask(int[] a, ExecutorService threadPool) {
        SERIAL_THRESHOLD = Math.max(a.length / (Runtime.getRuntime().availableProcessors() * 4), 4096);
        pool = threadPool;
        latch = new CountDownLatch(a.length);
        this.a = a;
    }

    public final void waitUntilSorted() throws InterruptedException {
        latch.await();
    }

    @Override
    public void run() {
        execute(new QuicksortSubTask(this));
    }

    final int getTaskSize() {
        return a.length;
    }

    private void execute(Runnable runnable) {
        pool.execute(runnable);
    }

    /**
     * Defines bounded region (sub-array) of array to sort.
     */
    private static class QuicksortSubTask implements Runnable {
        private final int left;
        private final int right;

        private final LatchQuicksortTask task;

        QuicksortSubTask(LatchQuicksortTask task) {
            this(task, 0, task.getTaskSize() - 1);
        }

        private QuicksortSubTask(LatchQuicksortTask task, int left, int right) {
            this.left = left;
            this.right = right;
            this.task = task;
        }

        @Override
        public void run() {
            int pivotIndex = task.partitionOrSort(left, right);

            if (pivotIndex >= 0) {
                if (left < pivotIndex)
                    task.execute(new QuicksortSubTask(task, left, pivotIndex));
                if (pivotIndex + 1 < right)
                    task.execute(new QuicksortSubTask(task, pivotIndex + 1, right));
            }
        }

    }

    private int partitionOrSort(int left, int right) {
        int pivotIndex = -1;
        int sortedCount;

        if (right - left < SERIAL_THRESHOLD) {
            Arrays.sort(a, left, right + 1);
            sortedCount = right - left + 1;
        } else {
            pivotIndex = partition(a, left, right);
            sortedCount = (left == pivotIndex || right == pivotIndex + 1)?1:0;
        }

        latch.countDown(sortedCount);

        return pivotIndex;
    }
}