package pmbauer.parallel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static pmbauer.parallel.Quicksort.partition;

public class ForkJoinQuicksortTask extends RecursiveAction {
    private static final int SERIAL_THRESHOLD = 0x1000;

    private final int[] a;
    private final int left;
    private final int right;

    public ForkJoinQuicksortTask(int[] a) {
        this(a, 0, a.length - 1);
    }

    private ForkJoinQuicksortTask(int[] a, int left, int right) {
        this.a = a;
        this.left = left;
        this.right = right;
    }

    @Override
    protected void compute() {
        if (right - left < SERIAL_THRESHOLD) {
            Arrays.sort(a, left, right + 1);
        } else {
            int pivotIndex = partition(a, left, right);

            List<ForkJoinQuicksortTask> parts = new LinkedList<>();

            if (left < pivotIndex)
                parts.add(new ForkJoinQuicksortTask(a, left, pivotIndex));
            if (pivotIndex + 1 < right)
                parts.add(new ForkJoinQuicksortTask(a, pivotIndex + 1, right));

            invokeAll(parts);
        }
    }
}