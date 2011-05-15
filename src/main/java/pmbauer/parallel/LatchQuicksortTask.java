package pmbauer.parallel;

import pmbauer.concurrent.CountDownLatch;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static pmbauer.parallel.Quicksort.partition;

/**
 * Root task to encapsulate:
 * <ul>
 * <li>array to sort</li>
 * <li>sorting threshold : computed in the constructor; sub-tasks smaller than this are serially sorted</li>
 * <li>latch : synchronization object that signals when the work is complete</li>
 * <li>sub-task spawning</li>
 * </ul>
 */
public class LatchQuicksortTask implements Runnable {

    /**
     * Defines bounded region (sub-array) of array to sort.
     */
    private static class QuicksortSubTask implements Runnable {
        private final int left;
        private final int right;
        private final LatchQuicksortTask root;

        QuicksortSubTask(LatchQuicksortTask task) {
            this(task, 0, task.getTaskSize() - 1);
        }

        QuicksortSubTask(LatchQuicksortTask rootTask, int left, int right) {
            this.left = left;
            this.right = right;
            this.root = rootTask;
        }

        @Override
        public void run() {
            int pivotIndex = root.partitionOrSort(left, right);

            if (pivotIndex >= 0) {
                if (left < pivotIndex)
                    root.execute(new QuicksortSubTask(root, left, pivotIndex));
                if (pivotIndex + 1 < right)
                    root.execute(new QuicksortSubTask(root, pivotIndex + 1, right));
            }
        }

    }

    private final int serialThreshold;
    private final ExecutorService pool;
    private final CountDownLatch latch;
    private final int[] a;

    public LatchQuicksortTask(int[] a, ExecutorService threadPool) {
        serialThreshold = Math.max(a.length / (Runtime.getRuntime().availableProcessors() * 4), 4096);
        pool = threadPool;
        latch = new CountDownLatch(a.length);
        this.a = a;
    }

    public final void waitUntilSorted() throws InterruptedException {
        latch.await();
    }

    final int getTaskSize() {
        return a.length;
    }

    @Override
    public void run() {
        execute(new QuicksortSubTask(this));
    }

    private void execute(Runnable runnable) {
        pool.execute(runnable);
    }

    /*
     * If the array sub-section [left,right] is small enough, fully sorts the sub-section and returns -1.
     * Otherwise, it partitions the subsection and returns an index within [left,right] such that
     * all values at indices <= returned index are less than values at indices > returned index.
     */
    private int partitionOrSort(int left, int right) {
        int pivotIndex = -1;
        int sortedCount;

        if (serialThresholdMet(left, right)) {
            Arrays.sort(a, left, right + 1);
            sortedCount = right - left + 1;
        } else {
            pivotIndex = partition(a, left, right);
            sortedCount = (left == pivotIndex || right == pivotIndex + 1) ? 1 : 0;
        }

        latch.countDown(sortedCount);

        return pivotIndex;
    }

    private boolean serialThresholdMet(int left, int right) {
        return right - left < serialThreshold;
    }
}