package pmbauer.parallel;

/**
 * Container for the arrays used to test MTQuick
 *
 * @author pbauer
 */
class SortFixture implements Cloneable {
    int[] array;
    final String description;

    SortFixture(String description, int[] array) {
        this.description = description;
        this.array = array;
    }

    /**
     * @return true if array is sorted in non-decreasing order
     */
    boolean isSorted() {
        for (int i = 1; i < array.length; i++)
            if (array[i] < array[i - 1])
                return false;
        return true;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        SortFixture clone = (SortFixture) super.clone();
        clone.array = array.clone();
        return clone;
    }
}