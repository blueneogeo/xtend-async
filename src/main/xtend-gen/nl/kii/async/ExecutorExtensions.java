package nl.kii.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseFuture;
import nl.kii.promise.Task;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class ExecutorExtensions {
  /**
   * Convert a promise into a Future.
   * Promises are non-blocking. However you can convert to a Future
   * if you must block and wait for a promise to resolve.
   * <pre>
   * val result = promise.future.get // blocks code until the promise is fulfilled
   */
  public static <T extends Object> Future<T> future(final IPromise<T> promise) {
    return new PromiseFuture<T>(promise);
  }
  
  /**
   * Execute the callable in the background and return as a promise.
   * Lets you specify the executorservice to run on.
   * <pre>
   * val service = Executors.newSingleThreadExecutor
   * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
   */
  public static <T extends Object> IPromise<T> promise(final ExecutorService service, final Callable<T> callable) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final Runnable _function = new Runnable() {
        public void run() {
          try {
            final T result = callable.call();
            promise.set(result);
          } catch (final Throwable _t) {
            if (_t instanceof Throwable) {
              final Throwable t = (Throwable)_t;
              promise.error(t);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      final Runnable processor = _function;
      service.submit(processor);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Execute the runnable in the background and return as a promise.
   * Lets you specify the executorservice to run on.
   * <pre>
   * val service = Executors.newSingleThreadExecutor
   * service.promise [| doSomeHeavyLifting ].then [ println('done!') ]
   */
  public static Task task(final ExecutorService service, final Runnable runnable) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      final Runnable _function = new Runnable() {
        public void run() {
          try {
            runnable.run();
            task.complete();
          } catch (final Throwable _t) {
            if (_t instanceof Throwable) {
              final Throwable t = (Throwable)_t;
              task.error(t);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      final Runnable processor = _function;
      service.submit(processor);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a timer stream, that pushes out the time in ms since starting, every periodMs ms.
   * Note: this breaks the single threaded model!
   */
  public static Stream<Long> streamEvery(final ScheduledExecutorService scheduler, final int periodMs) {
    return ExecutorExtensions.streamEvery(scheduler, periodMs, (-1));
  }
  
  /**
   * Create a timer stream, that pushes out the time in ms since starting, every periodMs ms.
   * It will keep doing this for forPeriodMs time. Set forPeriodMs to -1 to stream forever.
   * Note: this breaks the single threaded model!
   */
  public static Stream<Long> streamEvery(final ScheduledExecutorService scheduler, final int periodMs, final int forPeriodMs) {
    Stream<Long> _xblockexpression = null;
    {
      final AtomicReference<ScheduledFuture<?>> task = new AtomicReference<ScheduledFuture<?>>();
      final Stream<Long> newStream = StreamExtensions.<Long>stream(long.class);
      final long start = System.currentTimeMillis();
      final Runnable _function = new Runnable() {
        public void run() {
          final long now = System.currentTimeMillis();
          final boolean expired = ((forPeriodMs > 0) && ((now - start) > forPeriodMs));
          boolean _and = false;
          Boolean _isOpen = newStream.isOpen();
          if (!(_isOpen).booleanValue()) {
            _and = false;
          } else {
            _and = (!expired);
          }
          if (_and) {
            newStream.push(Long.valueOf((now - start)));
          } else {
            ScheduledFuture<?> _get = task.get();
            _get.cancel(false);
          }
        }
      };
      final Runnable pusher = _function;
      ScheduledFuture<?> _scheduleAtFixedRate = scheduler.scheduleAtFixedRate(pusher, 0, periodMs, TimeUnit.MILLISECONDS);
      task.set(_scheduleAtFixedRate);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Push a value onto the stream from the parent stream every periodMs milliseconds.
   * Note: It requires a scheduled executor for the scheduling. This breaks the singlethreaded model.
   */
  public static <T extends Object> Stream<T> every(final Stream<T> stream, final int periodMs, final ScheduledExecutorService executor) {
    Stream<Long> _streamEvery = ExecutorExtensions.streamEvery(executor, periodMs);
    return StreamExtensions.<T>synchronizeWith(stream, _streamEvery);
  }
}
