package nl.kii.stream;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseFuture;
import nl.kii.stream.Task;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class PromiseExtensions {
  /**
   * Create a promise of the given type
   */
  public static <T extends Object> Promise<T> promise(final Class<T> type) {
    return new Promise<T>();
  }
  
  /**
   * Create a promise of a list of the given type
   */
  public static <T extends Object> Promise<List<T>> promiseList(final Class<T> type) {
    return new Promise<List<T>>();
  }
  
  /**
   * Create a promise of a map of the given key and value types
   */
  public static <K extends Object, V extends Object> Promise<Map<K, V>> promiseMap(final Pair<Class<K>, Class<V>> type) {
    return new Promise<Map<K, V>>();
  }
  
  /**
   * Create a promise that immediately resolves to the passed value
   */
  public static <T extends Object> Promise<T> promise(final T value) {
    return new Promise<T>(value);
  }
  
  public static <T extends Object> Promise<T> promise(final Class<T> type, final Procedure1<? super Promise<T>> blockThatFulfillsPromise) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = PromiseExtensions.<T>promise(type);
      try {
        blockThatFulfillsPromise.apply(promise);
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          promise.error(t);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Promise<List<T>> promiseList(final Class<T> type, final Procedure1<? super Promise<List<T>>> blockThatFulfillsPromise) {
    Promise<List<T>> _xblockexpression = null;
    {
      final Promise<List<T>> promise = PromiseExtensions.<T>promiseList(type);
      try {
        blockThatFulfillsPromise.apply(promise);
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          promise.error(t);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  public static <K extends Object, V extends Object> Promise<Map<K, V>> promiseMap(final Pair<Class<K>, Class<V>> type, final Procedure1<? super Promise<Map<K, V>>> blockThatFulfillsPromise) {
    Promise<Map<K, V>> _xblockexpression = null;
    {
      final Promise<Map<K, V>> promise = PromiseExtensions.<K, V>promiseMap(type);
      try {
        blockThatFulfillsPromise.apply(promise);
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          promise.error(t);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  public static Task task(final Procedure1<? super Task> blockThatPerformsTask) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      try {
        blockThatPerformsTask.apply(task);
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          task.error(t);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Tell the task it went wrong
   */
  public static Task error(final Task task, final String message) {
    Exception _exception = new Exception(message);
    Promise<Boolean> _error = task.error(_exception);
    return ((Task) _error);
  }
  
  /**
   * Tell the promise it went wrong
   */
  public static <T extends Object> Promise<T> error(final Promise<T> promise, final String message) {
    Exception _exception = new Exception(message);
    return promise.error(_exception);
  }
  
  /**
   * Fulfill a promise
   */
  public static <T extends Object> Promise<T> operator_doubleGreaterThan(final T value, final Promise<T> promise) {
    Promise<T> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Fulfill a promise
   */
  public static <T extends Object> Promise<T> operator_doubleLessThan(final Promise<T> promise, final T value) {
    Promise<T> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a new promise from an existing promise,
   * that transforms the value of the promise
   * once the existing promise is resolved.
   */
  public static <T extends Object, R extends Object> Promise<R> map(final Promise<T> promise, final Function1<? super T, ? extends R> mappingFn) {
    Promise<R> _xblockexpression = null;
    {
      final Promise<R> newPromise = new Promise<R>(promise);
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          R _apply = mappingFn.apply(it);
          newPromise.set(_apply);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Flattens a promise of a promise to directly a promise.
   */
  public static <T extends Object> Promise<T> flatten(final Promise<Promise<T>> promise) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> newPromise = new Promise<T>(promise);
      final Procedure1<Promise<T>> _function = new Procedure1<Promise<T>>() {
        public void apply(final Promise<T> it) {
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<T> _onError = it.onError(_function);
          final Procedure1<T> _function_1 = new Procedure1<T>() {
            public void apply(final T it) {
              newPromise.set(it);
            }
          };
          _onError.then(_function_1);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Alias for flatten, turns a promise of a promise into a promise
   */
  public static <T extends Object> Promise<T> resolve(final Promise<Promise<T>> promise) {
    return PromiseExtensions.<T>flatten(promise);
  }
  
  /**
   * Create a new promise that listenes to this promise
   */
  public static <T extends Object> Promise<T> fork(final Promise<T> promise) {
    final Function1<T, T> _function = new Function1<T, T>() {
      public T apply(final T it) {
        return it;
      }
    };
    return PromiseExtensions.<T, T>map(promise, _function);
  }
  
  /**
   * Convert a promise into a Future.
   * Promises are non-blocking. However you can convert to a Future
   * if you must block and wait for a promise to resolve.
   * <pre>
   * val result = promise.future.get // blocks code until the promise is fulfilled
   */
  public static <T extends Object> Future<T> future(final Promise<T> promise) {
    return new PromiseFuture<T>(promise);
  }
  
  /**
   * Execute the callable in the background and return as a promise.
   * Lets you specify the executorservice to run on.
   * <pre>
   * val service = Executors.newSingleThreadExecutor
   * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
   */
  public static <T extends Object> Promise<T> async(final ExecutorService service, final Callable<T> callable) {
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
  public static Task run(final ExecutorService service, final Runnable runnable) {
    final Procedure1<Task> _function = new Procedure1<Task>() {
      public void apply(final Task task) {
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
      }
    };
    return PromiseExtensions.task(_function);
  }
}
