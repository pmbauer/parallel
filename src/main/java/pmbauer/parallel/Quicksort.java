package pmbauer.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class Quicksort {

    public static int[] latchQuicksort(ExecutorService pool, int[] a) throws InterruptedException {
        LatchQuicksortTask sortingTask = new LatchQuicksortTask(a, pool);

        pool.execute(sortingTask);
        sortingTask.waitUntilSorted();

        return a;
    }

    public static int[] forkJoinQuicksort(ForkJoinPool pool, int[] a) {
        pool.invoke(new ForkJoinQuicksortTask(a));
        return a;
    }

    /**
     * Example invocation:
     * <pre>
     * {@code
     *  public static int[] sequentialQuicksort(int[] a, int left, int right) {
            while (left < right) {
                int pivotIndex = partition(a, left, right);
                sequentialQuicksort(a, left, pivotIndex);
                left = pivotIndex + 1;
            }

            return a;
        }
     * }
     * </pre>
     * @param a array to partition
     * @param left lower bound for partition
     * @param right upper bound for partition (inclusive)
     * @return pivot index - assert(a[i] < a[j]) for all i in {left <= i <= pivot}
     * and all j in {pivot < j <= right}
     * @see ForkJoinQuicksortTask
     * @see LatchQuicksortTask
     */
    public static int partition(int[] a, int left, int right) {
        // chose middle value of range for our pivot
        int pivotValue = a[(right + left + 1) / 2];

        --left;
        ++right;

        while (true) {
            do
                ++left;
            while (a[left] < pivotValue);

            do
                --right;
            while (a[right] > pivotValue);

            if (left < right) {
                int tmp = a[left];
                a[left] = a[right];
                a[right] = tmp;
            } else {
                return right;
            }
        }
    }
}