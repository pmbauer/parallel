package pmbauer.parallel;

import java.util.Random;

class SortTestUtils {
    
    /**
     * Returns an array full of random integers
     * @param size size of returned integer array
     * @return integer array of random integers
     */
    static int[] randomArray(int size) {
        // create array of random numbers
        int[] a = new int[size];
        Random r = new Random();

        for (int i = 0; i < size; i++) {
             a[i] = r.nextInt(size);
        }

        return a;
    }

    static int[] reverse(int[] a) {
        int left = 0;
        int right = a.length - 1;

        while (left < right) {
            int temp = a[left];
            a[left] = a[right];
            a[right] = temp;
            left++; right--;
        }

        return a;
    }

}