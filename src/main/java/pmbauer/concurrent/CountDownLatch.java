package pmbauer.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Identical to JDK {@link java.util.concurrent.CountDownLatch}, but adds {@link #countDown(int) }.
 * We do not inherit from the built-in JDK class because its private synchronization mechanism does
 * not efficiently support counting down an arbitrary value - only one value at a time
 * @see java.util.concurrent.CountDownLatch
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public class CountDownLatch {

    /**
     * Represents the count
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        public int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        public boolean tryReleaseShared(int releases) {
            // Non-blocking, decrements count, signaling on transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextCount = c-releases;
                if (compareAndSetState(c, nextCount))
                    return nextCount == 0;
            }
        }
    }

    private final Sync sync;

    /**
     * @see java.util.concurrent.CountDownLatch#CountDownLatch(int)
     */
    public CountDownLatch(int count) {
        if (count < 0)
            throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * @see java.util.concurrent.CountDownLatch#await()
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * @see java.util.concurrent.CountDownLatch#await(long, java.util.concurrent.TimeUnit)
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * @see java.util.concurrent.CountDownLatch#countDown()
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * Like {@link #countDown()}, but reduces the count by an arbitrary value.
     * @param count
     */
    public void countDown(int count) {
        sync.releaseShared(count);
    }

    /**
     * @see java.util.concurrent.CountDownLatch#getCount()
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * @see java.util.concurrent.CountDownLatch#toString()
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}