package nl.kii.stream;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseFuture;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
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
   * Create a promise that immediately resolves to the passed value
   */
  public static <T extends Object> Promise<T> promise(final T value) {
    return new Promise<T>(value);
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
  public static <T extends Object, R extends Object> Promise<R> map(final Promise<T> promise, final Function1<? super T,? extends R> mappingFn) {
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
   * Perform an async operation which returns a promise.
   * This allows you to chain multiple async methods, as
   * long as you let your closures return a Promise
   * <p>
   * Example:
   * <pre>
   * def Promise<User> loadUser(int userId)
   * def Promise<Result> uploadUser(User user)
   * def void showUploadResult(Result result)
   * 
   * loadUser(12)
   *    .async [ uploadUser ]
   *    .then [ showUploadResult ]
   * </pre>
   */
  public static <T extends Object, R extends Object> Promise<R> mapAsync(final Promise<T> promise, final Function1<? super T,? extends Promise<R>> promiseFn) {
    Promise<Promise<R>> _map = PromiseExtensions.<T, Promise<R>>map(promise, promiseFn);
    return PromiseExtensions.<R>flatten(_map);
  }
  
  public static <T extends Object, R extends Object> void thenAsync(final Promise<T> promise, final Function1<? super T,? extends Promise<R>> promiseFn) {
    Promise<R> _mapAsync = PromiseExtensions.<T, R>mapAsync(promise, promiseFn);
    final Procedure1<R> _function = new Procedure1<R>() {
      public void apply(final R it) {
      }
    };
    _mapAsync.then(_function);
  }
  
  /**
   * Create a new promise that listenes to this promise
   */
  public static <T extends Object> Promise<T> fork(final Promise<T> promise) {
    final Function1<T,T> _function = new Function1<T,T>() {
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
   * Easily run a procedure in the background and return a promise
   * promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
   */
  public static Promise<Void> promise(final Procedure0 procedure) {
    final Procedure0 _function = new Procedure0() {
      public void apply() {
        procedure.apply();
      }
    };
    return PromiseExtensions.promise(((Runnable) new Runnable() {
        public void run() {
          _function.apply();
        }
    }));
  }
  
  /**
   * Easily run a function in the background and return a promise
   * <pre>
   * promise [| doSomeHeavyLifting ].then [ println('done!') ]
   */
  public static <T extends Object> Promise<T> promise(final Function0<? extends T> function) {
    final Function0<T> _function = new Function0<T>() {
      public T apply() {
        return function.apply();
      }
    };
    return PromiseExtensions.<T>promise(((Callable<T>) new Callable<T>() {
        public T call() {
          return _function.apply();
        }
    }));
  }
  
  /**
   * Execute the callable in the background and return as a promise
   */
  public static <T extends Object> Promise<T> promise(final Callable<T> callable) {
    ExecutorService _newSingleThreadExecutor = Executors.newSingleThreadExecutor();
    return PromiseExtensions.<T>promise(_newSingleThreadExecutor, callable);
  }
  
  /**
   * Execute the runnable in the background and return as a promise
   */
  public static Promise<Void> promise(final Runnable runnable) {
    ExecutorService _newSingleThreadExecutor = Executors.newSingleThreadExecutor();
    return PromiseExtensions.promise(_newSingleThreadExecutor, runnable);
  }
  
  /**
   * Execute the callable in the background and return as a promise.
   * Lets you specify the executorservice to run on.
   * <pre>
   * val service = Executors.newSingleThreadExecutor
   * service.promise [| return doSomeHeavyLifting ].then [ println('result:' + it) ]
   */
  public static <T extends Object> Promise<T> promise(final ExecutorService service, final Callable<T> callable) {
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
  public static Promise<Void> promise(final ExecutorService service, final Runnable runnable) {
    Promise<Void> _xblockexpression = null;
    {
      final Promise<Void> promise = new Promise<Void>();
      final Runnable _function = new Runnable() {
        public void run() {
          try {
            runnable.run();
            promise.set(null);
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
}