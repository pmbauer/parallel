package pmbauer.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @deprecated use ThreadPoolExecutor instead
 * @see java.util.concurrent.ThreadPoolExecutor
 */
@Deprecated
public final class WorkerPool {
    private static final double DEFAULT_SIZING_COEFFICIENT = 1.5;

    private final BlockingQueue<WorkUnit> workStream = new LinkedBlockingQueue<>();
    private Thread[] workThreads = new Thread[0];

    private WorkerPool() {
    }

    public static WorkerPool newPool(int poolSize) {
        WorkerPool instance = new WorkerPool();
        instance.sizeThreadPool(poolSize);
        return instance;
    }

    public static WorkerPool newPool() {
        return newPool((int)
                (Runtime.getRuntime().availableProcessors() * DEFAULT_SIZING_COEFFICIENT));
    }

    public final boolean addWork(WorkUnit toDo) {
        return workStream.add(toDo);
    }

    public synchronized void sizeThreadPool(int numberOfThreads) {
        shutdown();

        // make sure threadCount is in range [1, MAX_INT)
        numberOfThreads = (numberOfThreads != 0) ? Math.abs(numberOfThreads)
                : 1;

        workThreads = new Thread[numberOfThreads];

        // create and start threads
        for (int i = 0; i < numberOfThreads; ++i) {
            workThreads[i] = new Thread(new Worker(this));
            workThreads[i].start();
        }
    }

    public synchronized void shutdown() {
        // Signal workers to clean-up and exit.
        // There still may be work in the workStream.
        for (Thread workThread : workThreads) {
            workThread.interrupt();
        }

        workThreads = new Thread[0];
    }

    private static final class Worker implements Runnable {
        private final WorkerPool pool;

        private Worker(WorkerPool pool) {
            this.pool = pool;
        }

        public void run() {
            WorkUnit wu;

            while (true) {
                // * consume a work unit
                // (blocks if the workStream is empty)
                try {
                    wu = pool.workStream.take();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                } // time to quit

                wu.consumeUnit(pool);
            }
        }

    }
}
