package nl.kii.promise;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseFuture;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

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
  
  /**
   * Create a promise of a pair
   */
  public static <K extends Object, V extends Object> Promise<Pair<K, V>> promisePair(final Pair<Class<K>, Class<V>> type) {
    return new Promise<Pair<K, V>>();
  }
  
  /**
   * Distribute work using an asynchronous method
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> IPromise<List<R>> distribute(final List<T> data, final int concurrency, final Function1<? super T, ? extends P> operationFn) {
    Stream<T> _stream = StreamExtensions.<T>stream(data);
    Stream<P> _map = StreamExtensions.<T, P>map(_stream, operationFn);
    Stream<R> _resolve = StreamExtensions.<R, Object>resolve(_map, concurrency);
    Stream<List<R>> _collect = StreamExtensions.<R>collect(_resolve);
    return StreamExtensions.<List<R>>first(_collect);
  }
  
  /**
   * Tell the promise it went wrong
   */
  public static <T extends Object> Promise<T> error(final IPromise<T> promise, final String message) {
    Exception _exception = new Exception(message);
    return promise.error(_exception);
  }
  
  /**
   * Tell the promise it went wrong, with the cause throwable
   */
  public static <T extends Object> Promise<T> error(final IPromise<T> promise, final String message, final Throwable cause) {
    Exception _exception = new Exception(message, cause);
    return promise.error(_exception);
  }
  
  /**
   * Fulfill a promise
   */
  public static <T extends Object> IPromise<T> operator_doubleGreaterThan(final T value, final IPromise<T> promise) {
    IPromise<T> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Fulfill a promise
   */
  public static <T extends Object> IPromise<T> operator_doubleLessThan(final IPromise<T> promise, final T value) {
    IPromise<T> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Convert a promise into a task
   */
  public static <T extends Object> Task toTask(final IPromise<T> promise) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      final Function1<T, Boolean> _function = new Function1<T, Boolean>() {
        public Boolean apply(final T it) {
          return Boolean.valueOf(true);
        }
      };
      Promise<Boolean> _map = PromiseExtensions.<T, Boolean>map(promise, _function);
      PromiseExtensions.<Boolean>forwardTo(_map, task);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a new promise from an existing promise,
   * that transforms the value of the promise
   * once the existing promise is resolved.
   */
  public static <T extends Object, R extends Object> Promise<R> map(final IPromise<T> promise, final Function1<? super T, ? extends R> mappingFn) {
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
   * Maps a promise of a pair to a new promise, passing the key and value of the incoming
   * promise as listener parameters.
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Promise<V2> map(final IPromise<Pair<K1, V1>> promise, final Function2<? super K1, ? super V1, ? extends V2> mappingFn) {
    final Function1<Pair<K1, V1>, V2> _function = new Function1<Pair<K1, V1>, V2>() {
      public V2 apply(final Pair<K1, V1> it) {
        K1 _key = it.getKey();
        V1 _value = it.getValue();
        return mappingFn.apply(_key, _value);
      }
    };
    return PromiseExtensions.<Pair<K1, V1>, V2>map(promise, _function);
  }
  
  /**
   * Maps just the values of a promise of a pair to a new promise
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Promise<Pair<K1, V2>> mapValue(final IPromise<Pair<K1, V1>> promise, final Function1<? super V1, ? extends V2> mappingFn) {
    final Function1<Pair<K1, V1>, Pair<K1, V2>> _function = new Function1<Pair<K1, V1>, Pair<K1, V2>>() {
      public Pair<K1, V2> apply(final Pair<K1, V1> it) {
        K1 _key = it.getKey();
        V1 _value = it.getValue();
        V2 _apply = mappingFn.apply(_value);
        return Pair.<K1, V2>of(_key, _apply);
      }
    };
    return PromiseExtensions.<Pair<K1, V1>, Pair<K1, V2>>map(promise, _function);
  }
  
  /**
   * Flattens a promise of a promise to directly a promise.
   */
  public static <R extends Object, P extends IPromise<R>> Promise<R> flatten(final IPromise<P> promise) {
    Promise<R> _xblockexpression = null;
    {
      final Promise<R> newPromise = new Promise<R>(promise);
      final Procedure1<P> _function = new Procedure1<P>() {
        public void apply(final P it) {
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<R> _onError = it.onError(_function);
          final Procedure1<R> _function_1 = new Procedure1<R>() {
            public void apply(final R it) {
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
   * Same as normal promise resolve, however this time for a pair of a key and a promise.
   */
  public static <K extends Object, R extends Object, P extends IPromise<R>> Promise<Pair<K, R>> flattenPair(final IPromise<Pair<K, P>> promise) {
    Promise<Pair<K, R>> _xblockexpression = null;
    {
      final Promise<Pair<K, R>> newPromise = new Promise<Pair<K, R>>(promise);
      final Procedure1<Pair<K, P>> _function = new Procedure1<Pair<K, P>>() {
        public void apply(final Pair<K, P> pair) {
          P _value = pair.getValue();
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<R> _onError = _value.onError(_function);
          final Procedure1<R> _function_1 = new Procedure1<R>() {
            public void apply(final R it) {
              K _key = pair.getKey();
              Pair<K, R> _mappedTo = Pair.<K, R>of(_key, it);
              newPromise.set(_mappedTo);
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
   * Performs a flatmap, which is a combination of map and flatten
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> IPromise<R> flatMap(final IPromise<T> promise, final Function1<? super T, ? extends P> promiseFn) {
    final Function1<T, P> _function = new Function1<T, P>() {
      public P apply(final T it) {
        return promiseFn.apply(it);
      }
    };
    Promise<P> _map = PromiseExtensions.<T, P>map(promise, _function);
    return PromiseExtensions.<R, P>flatten(_map);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<R> flatMap(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends P> promiseFn) {
    final Function1<Pair<K, T>, P> _function = new Function1<Pair<K, T>, P>() {
      public P apply(final Pair<K, T> it) {
        K _key = it.getKey();
        T _value = it.getValue();
        return promiseFn.apply(_key, _value);
      }
    };
    Promise<P> _map = PromiseExtensions.<Pair<K, T>, P>map(promise, _function);
    return PromiseExtensions.<R, P>flatten(_map);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<Pair<K, R>> flatMapPair(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends Pair<K, P>> promiseFn) {
    final Function1<Pair<K, T>, Pair<K, P>> _function = new Function1<Pair<K, T>, Pair<K, P>>() {
      public Pair<K, P> apply(final Pair<K, T> it) {
        K _key = it.getKey();
        T _value = it.getValue();
        return promiseFn.apply(_key, _value);
      }
    };
    Promise<Pair<K, P>> _map = PromiseExtensions.<Pair<K, T>, Pair<K, P>>map(promise, _function);
    return PromiseExtensions.<K, R, P>flattenPair(_map);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<Pair<K, R>> flatMapPair(final IPromise<T> promise, final Function1<? super T, ? extends Pair<K, P>> promiseFn) {
    final Function1<T, Pair<K, P>> _function = new Function1<T, Pair<K, P>>() {
      public Pair<K, P> apply(final T it) {
        return promiseFn.apply(it);
      }
    };
    Promise<Pair<K, P>> _map = PromiseExtensions.<T, Pair<K, P>>map(promise, _function);
    return PromiseExtensions.<K, R, P>flattenPair(_map);
  }
  
  /**
   * When the promise gives a result, call the function that returns another promise and
   * return that promise so you can chain and continue. Any thrown errors will be caught
   * and passed down the chain so you can catch them at the bottom.
   * 
   * Internally, this method calls flatMap. However you use this method call to indicate
   * that the promiseFn will create sideeffects.
   * <p>
   * Example:
   * <pre>
   * loadUser
   *   .thenAsync [ checkCredentialsAsync ]
   *   .thenAsync [ signinUser ]
   *   .onError [ setErrorMessage('could not sign you in') ]
   *   .then [ println('success!') ]
   * </pre>
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> IPromise<R> thenAsync(final IPromise<T> promise, final Function1<? super T, ? extends P> promiseFn) {
    return PromiseExtensions.<T, R, P>flatMap(promise, promiseFn);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<R> thenAsync(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends P> promiseFn) {
    return PromiseExtensions.<T, R, K, P>flatMap(promise, promiseFn);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<Pair<K, R>> thenAsyncPair(final IPromise<T> promise, final Function1<? super T, ? extends Pair<K, P>> promiseFn) {
    return PromiseExtensions.<T, R, K, P>flatMapPair(promise, promiseFn);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<Pair<K, R>> thenAsyncPair(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends Pair<K, P>> promiseFn) {
    return PromiseExtensions.<T, R, K, P>flatMapPair(promise, promiseFn);
  }
  
  /**
   * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
   * See chain2() for example of how to use.
   */
  public static <K extends Object, V extends Object> void then(final IPromise<Pair<K, V>> promise, final Procedure2<? super K, ? super V> listener) {
    final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
      public void apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value);
      }
    };
    promise.then(_function);
  }
  
  /**
   * Fork a single promise into a list of promises
   * Note that the original promise is then being listened to and you
   * can no longer perform .then and .onError on it.
   */
  public static <T extends Object> IPromise<T>[] fork(final IPromise<T> promise, final int amount) {
    IPromise<T>[] _xblockexpression = null;
    {
      final IPromise<T>[] promises = new IPromise[amount];
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable t) {
          final Procedure1<IPromise<T>> _function = new Procedure1<IPromise<T>>() {
            public void apply(final IPromise<T> p) {
              p.error(t);
            }
          };
          IterableExtensions.<IPromise<T>>forEach(((Iterable<IPromise<T>>)Conversions.doWrapArray(promises)), _function);
        }
      };
      Promise<T> _onError = promise.onError(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T value) {
          final Procedure1<IPromise<T>> _function = new Procedure1<IPromise<T>>() {
            public void apply(final IPromise<T> p) {
              p.set(value);
            }
          };
          IterableExtensions.<IPromise<T>>forEach(((Iterable<IPromise<T>>)Conversions.doWrapArray(promises)), _function);
        }
      };
      _onError.then(_function_1);
      _xblockexpression = promises;
    }
    return _xblockexpression;
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <T extends Object> void forwardTo(final IPromise<T> promise, final IPromise<T> existingPromise) {
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        existingPromise.apply(it);
      }
    };
    Promise<T> _always = promise.always(_function);
    final Procedure1<T> _function_1 = new Procedure1<T>() {
      public void apply(final T it) {
      }
    };
    _always.then(_function_1);
  }
  
  /**
   * Create a stream of values out of a Promise of a list. If the promise throws an error,
   */
  public static <T extends Object, T2 extends Iterable<T>> Stream<T> stream(final IPromise<T2> promise) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          newStream.error(it);
        }
      };
      Promise<T2> _onError = promise.onError(_function);
      final Procedure1<T2> _function_1 = new Procedure1<T2>() {
        public void apply(final T2 it) {
          Stream<T> _stream = StreamExtensions.<T>stream(it);
          StreamExtensions.<T>forwardTo(_stream, newStream);
        }
      };
      _onError.then(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
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
  public static <T extends Object> IPromise<T> async(final ExecutorService service, final Callable<T> callable) {
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
}
