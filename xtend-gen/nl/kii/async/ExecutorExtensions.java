package nl.kii.async;

import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseFuture;
import nl.kii.promise.Task;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.internal.SubStream;

public class ExecutorExtensions {
  /**
   * Convert a promise into a Future.
   * Promises are non-blocking. However you can convert to a Future
   * if you must block and wait for a promise to resolve.
   * <pre>
   * val result = promise.future.get // blocks code until the promise is fulfilled
   */
  public static <R extends java.lang.Object, T extends java.lang.Object> /* Future<T> */Object future(final IPromise<R, T> promise) {
    return new PromiseFuture<T>(promise);
  }
  
  /**
   * Execute the callable in the background and return as a promise.
   * Lets you specify the executorservice to run on.
   * <pre>
   * val service = Executors.newSingleThreadExecutor
   * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
   */
  public static <T extends java.lang.Object> Promise<T> promise(final /* ExecutorService */Object service, final /* Callable<T> */Object callable) {
    throw new Error("Unresolved compilation problems:"
      + "\nRunnable cannot be resolved to a type."
      + "\nThrowable cannot be resolved to a type."
      + "\nNo exception of type Throwable can be thrown; an exception type must be a subclass of Throwable"
      + "\ncall cannot be resolved"
      + "\nsubmit cannot be resolved");
  }
  
  /**
   * Execute the runnable in the background and return as a promise.
   * Lets you specify the executorservice to run on.
   * <pre>
   * val service = Executors.newSingleThreadExecutor
   * service.promise [| doSomeHeavyLifting ].then [ println('done!') ]
   */
  public static Task task(final /* ExecutorService */Object service, final /* Runnable */Object runnable) {
    throw new Error("Unresolved compilation problems:"
      + "\nRunnable cannot be resolved to a type."
      + "\nThrowable cannot be resolved to a type."
      + "\nNo exception of type Throwable can be thrown; an exception type must be a subclass of Throwable"
      + "\nrun cannot be resolved"
      + "\nsubmit cannot be resolved");
  }
  
  public static /*  */Object scheduler(final /* ScheduledExecutorService */Object executor) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field TimeUnit is undefined for the type ExecutorExtensions"
      + "\nschedule cannot be resolved"
      + "\nMILLISECONDS cannot be resolved");
  }
  
  /**
   * Create a timer stream, that pushes out the time in ms since starting, every periodMs ms.
   * Note: this breaks the single threaded model!
   */
  public static /* Stream<Long> */Object streamEvery(final /* ScheduledExecutorService */Object scheduler, final int periodMs) {
    throw new Error("Unresolved compilation problems:"
      + "\n- cannot be resolved.");
  }
  
  /**
   * Create a timer stream, that pushes out the time in ms since starting, every periodMs ms.
   * It will keep doing this for forPeriodMs time. Set forPeriodMs to -1 to stream forever.
   * Note: this breaks the single threaded model!
   */
  public static /* Stream<Long> */Object streamEvery(final /* ScheduledExecutorService */Object scheduler, final int periodMs, final int forPeriodMs) {
    throw new Error("Unresolved compilation problems:"
      + "\nRunnable cannot be resolved to a type."
      + "\nAtomicReference cannot be resolved."
      + "\nThe method or field System is undefined for the type ExecutorExtensions"
      + "\nThe method or field System is undefined for the type ExecutorExtensions"
      + "\n> cannot be resolved."
      + "\nThe method or field MILLISECONDS is undefined for the type ExecutorExtensions"
      + "\nScheduledFuture cannot be resolved to a type."
      + "\nstream cannot be resolved"
      + "\ncurrentTimeMillis cannot be resolved"
      + "\ncurrentTimeMillis cannot be resolved"
      + "\n&& cannot be resolved"
      + "\n- cannot be resolved"
      + "\n> cannot be resolved"
      + "\nopen cannot be resolved"
      + "\n&& cannot be resolved"
      + "\n! cannot be resolved"
      + "\npush cannot be resolved"
      + "\n- cannot be resolved"
      + "\nget cannot be resolved"
      + "\ncancel cannot be resolved"
      + "\nset cannot be resolved"
      + "\nscheduleAtFixedRate cannot be resolved");
  }
  
  /**
   * Push a value onto the stream from the parent stream every periodMs milliseconds.
   * Note: It requires a scheduled executor for the scheduling. This breaks the singlethreaded model.
   */
  public static <R extends java.lang.Object, T extends java.lang.Object> SubStream<R, T> every(final IStream<R, T> stream, final int periodMs, final /* ScheduledExecutorService */Object executor) {
    throw new Error("Unresolved compilation problems:"
      + "\nstreamEvery cannot be resolved");
  }
}
