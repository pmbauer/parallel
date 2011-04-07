package pmbauer.concurrent;

/**
 * @deprecated Use java.lang.Runnable instead
 */
@Deprecated
public interface WorkUnit {
    void consumeUnit(WorkerPool pool);
}
