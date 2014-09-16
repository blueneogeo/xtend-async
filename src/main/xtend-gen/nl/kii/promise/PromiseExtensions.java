package nl.kii.promise;

import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseException;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
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
   * Create a promise that immediately resolves to the passed value.
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
  public static <T extends Object, R extends Object, P extends IPromise<R>> IPromise<List<R>> call(final List<T> data, final int concurrency, final Function1<? super T, ? extends P> operationFn) {
    Stream<T> _stream = StreamExtensions.<T>stream(data);
    Stream<P> _map = StreamExtensions.<T, P>map(_stream, operationFn);
    Stream<R> _resolve = StreamExtensions.<R, Object>resolve(_map, concurrency);
    Stream<List<R>> _collect = StreamExtensions.<R>collect(_resolve);
    IPromise<List<R>> _first = StreamExtensions.<List<R>>first(_collect);
    final Procedure1<IPromise<List<R>>> _function = new Procedure1<IPromise<List<R>>>() {
      public void apply(final IPromise<List<R>> it) {
        it.setOperation((("call(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      }
    };
    return ObjectExtensions.<IPromise<List<R>>>operator_doubleArrow(_first, _function);
  }
  
  /**
   * Shortcut for quickly creating a completed task
   */
  public static Task complete() {
    Task _task = new Task();
    final Procedure1<Task> _function = new Procedure1<Task>() {
      public void apply(final Task it) {
        it.complete();
      }
    };
    return ObjectExtensions.<Task>operator_doubleArrow(_task, _function);
  }
  
  /**
   * Shortcut for quickly creating a promise with an error
   */
  public static <T extends Object> IPromise<T> error(final String message) {
    Promise<T> _promise = new Promise<T>();
    final Procedure1<Promise<T>> _function = new Procedure1<Promise<T>>() {
      public void apply(final Promise<T> it) {
        PromiseExtensions.<T>error(message);
      }
    };
    return ObjectExtensions.<Promise<T>>operator_doubleArrow(_promise, _function);
  }
  
  /**
   * Create a new Task that completes when all wrapped tasks are completed.
   * Errors created by the tasks are propagated into the resulting task.
   */
  public static Task all(final IPromise<?>... promises) {
    List<IPromise<?>> _list = IterableExtensions.<IPromise<?>>toList(((Iterable<IPromise<?>>)Conversions.doWrapArray(promises)));
    return PromiseExtensions.all(_list);
  }
  
  /**
   * Create a new Task that completes when all wrapped tasks are completed.
   * Errors created by the tasks are propagated into the resulting task.
   */
  public static Task all(final Iterable<? extends IPromise<?>> promises) {
    final Function1<IPromise<?>, Task> _function = new Function1<IPromise<?>, Task>() {
      public Task apply(final IPromise<?> it) {
        return PromiseExtensions.asTask(it);
      }
    };
    Iterable<Task> _map = IterableExtensions.map(promises, _function);
    Stream<Task> _stream = StreamExtensions.<Task>stream(_map);
    final Function1<Task, Task> _function_1 = new Function1<Task, Task>() {
      public Task apply(final Task it) {
        return it;
      }
    };
    Stream<Boolean> _call = StreamExtensions.<Task, Boolean, Task>call(_stream, _function_1);
    Stream<List<Boolean>> _collect = StreamExtensions.<Boolean>collect(_call);
    IPromise<List<Boolean>> _first = StreamExtensions.<List<Boolean>>first(_collect);
    return PromiseExtensions.asTask(_first);
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <T extends Object, P extends IPromise<T>> Task any(final P... promises) {
    List<P> _list = IterableExtensions.<P>toList(((Iterable<P>)Conversions.doWrapArray(promises)));
    return PromiseExtensions.<T>any(_list);
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <T extends Object> Task any(final List<? extends IPromise<T>> promises) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      for (final IPromise<T> promise : promises) {
        final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            task.error(it);
          }
        };
        IPromise<T> _onError = promise.onError(_function);
        final Procedure1<T> _function_1 = new Procedure1<T>() {
          public void apply(final T it) {
            task.complete();
          }
        };
        _onError.then(_function_1);
      }
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Always call onResult, whether the promise has been either fulfilled or had an error.
   */
  public static <T extends Object> IPromise<T> always(final IPromise<T> promise, final Procedure1<Entry<T>> resultFn) {
    IPromise<T> _xblockexpression = null;
    {
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(it);
          resultFn.apply(_error);
        }
      };
      promise.onError(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          Value<T> _value = new Value<T>(it);
          resultFn.apply(_value);
        }
      };
      promise.then(_function_1);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Tell the promise it went wrong
   */
  public static <T extends Object> IPromise<T> error(final IPromise<T> promise, final String message) {
    Exception _exception = new Exception(message);
    return promise.error(_exception);
  }
  
  /**
   * Tell the promise it went wrong, with the cause throwable
   */
  public static <T extends Object> IPromise<T> error(final IPromise<T> promise, final String message, final Throwable cause) {
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
   * All/And
   */
  public static Task operator_and(final IPromise<?> p1, final IPromise<?> p2) {
    return PromiseExtensions.all(p1, p2);
  }
  
  /**
   * Any/Or
   */
  public static <T extends Object> Task operator_or(final IPromise<T> p1, final IPromise<T> p2) {
    return PromiseExtensions.<T, IPromise<T>>any(p1, p2);
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
      final Procedure1<Promise<R>> _function_1 = new Procedure1<Promise<R>>() {
        public void apply(final Promise<R> it) {
          it.setOperation("map");
        }
      };
      _xblockexpression = ObjectExtensions.<Promise<R>>operator_doubleArrow(newPromise, _function_1);
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
    Promise<V2> _map = PromiseExtensions.<Pair<K1, V1>, V2>map(promise, _function);
    final Procedure1<Promise<V2>> _function_1 = new Procedure1<Promise<V2>>() {
      public void apply(final Promise<V2> it) {
        it.setOperation("map");
      }
    };
    return ObjectExtensions.<Promise<V2>>operator_doubleArrow(_map, _function_1);
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
    Promise<Pair<K1, V2>> _map = PromiseExtensions.<Pair<K1, V1>, Pair<K1, V2>>map(promise, _function);
    final Procedure1<Promise<Pair<K1, V2>>> _function_1 = new Procedure1<Promise<Pair<K1, V2>>>() {
      public void apply(final Promise<Pair<K1, V2>> it) {
        it.setOperation("mapValue");
      }
    };
    return ObjectExtensions.<Promise<Pair<K1, V2>>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * Maps errors back into values.
   * Good for alternative path resolving and providing defaults.
   */
  public static <T extends Object> Promise<T> mapError(final IPromise<T> promise, final Function1<? super Throwable, ? extends T> mappingFn) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> newPromise = new Promise<T>();
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          newPromise.set(it);
        }
      };
      Task _then = promise.then(_function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          try {
            T _apply = mappingFn.apply(it);
            newPromise.set(_apply);
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              newPromise.error(e);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      _then.onError(_function_1);
      final Procedure1<Promise<T>> _function_2 = new Procedure1<Promise<T>>() {
        public void apply(final Promise<T> it) {
          it.setOperation("onErrorMap");
        }
      };
      _xblockexpression = ObjectExtensions.<Promise<T>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Maps errors back into values.
   * Good for alternative path resolving and providing defaults.
   */
  public static <T extends Object> Promise<T> onErrorCall(final IPromise<T> promise, final Function1<? super Throwable, ? extends Promise<T>> mappingFn) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<Promise<T>> newPromise = new Promise<Promise<T>>();
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          Promise<T> _promise = PromiseExtensions.<T>promise(it);
          newPromise.set(_promise);
        }
      };
      Task _then = promise.then(_function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          try {
            Promise<T> _apply = mappingFn.apply(it);
            newPromise.set(_apply);
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              newPromise.error(e);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      _then.onError(_function_1);
      Promise<T> _resolve = PromiseExtensions.<T, Promise<T>>resolve(newPromise);
      final Procedure1<Promise<T>> _function_2 = new Procedure1<Promise<T>>() {
        public void apply(final Promise<T> it) {
          it.setOperation("onErrorCall");
        }
      };
      _xblockexpression = ObjectExtensions.<Promise<T>>operator_doubleArrow(_resolve, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Flattens a promise of a promise to directly a promise.
   */
  public static <R extends Object, P extends IPromise<R>> Promise<R> flatten(final IPromise<P> promise) {
    Promise<R> _resolve = PromiseExtensions.<R, P>resolve(promise);
    final Procedure1<Promise<R>> _function = new Procedure1<Promise<R>>() {
      public void apply(final Promise<R> it) {
        it.setOperation("flatten");
      }
    };
    return ObjectExtensions.<Promise<R>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Create a stream out of a promise of a stream.
   */
  public static <P extends IPromise<Stream<T>>, T extends Object> Stream<T> toStream(final P promise) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          newStream.error(it);
        }
      };
      IPromise<Stream<T>> _onError = promise.onError(_function);
      final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
        public void apply(final Stream<T> s) {
          StreamExtensions.<T>pipe(s, newStream);
        }
      };
      _onError.then(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Resolve a promise of a promise to directly a promise.
   * Alias for Promise.flatten, added for consistent syntax with streams
   */
  public static <R extends Object, P extends IPromise<R>> Promise<R> resolve(final IPromise<P> promise) {
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
          IPromise<R> _onError = it.onError(_function);
          final Procedure1<R> _function_1 = new Procedure1<R>() {
            public void apply(final R it) {
              newPromise.set(it);
            }
          };
          _onError.then(_function_1);
        }
      };
      promise.then(_function);
      final Procedure1<Promise<R>> _function_1 = new Procedure1<Promise<R>>() {
        public void apply(final Promise<R> it) {
          it.setOperation("resolve");
        }
      };
      _xblockexpression = ObjectExtensions.<Promise<R>>operator_doubleArrow(newPromise, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Same as normal promise resolve, however this time for a pair of a key and a promise.
   * Similar to Stream.resolveValue.
   */
  public static <K extends Object, R extends Object, P extends IPromise<R>> Promise<Pair<K, R>> resolveValue(final IPromise<Pair<K, P>> promise) {
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
          IPromise<R> _onError = _value.onError(_function);
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
      final Procedure1<Promise<Pair<K, R>>> _function_1 = new Procedure1<Promise<Pair<K, R>>>() {
        public void apply(final Promise<Pair<K, R>> it) {
          it.setOperation("resolveValue");
        }
      };
      _xblockexpression = ObjectExtensions.<Promise<Pair<K, R>>>operator_doubleArrow(newPromise, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Performs a flatmap, which is a combination of map and flatten/resolve
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> IPromise<R> flatMap(final IPromise<T> promise, final Function1<? super T, ? extends P> promiseFn) {
    Promise<P> _map = PromiseExtensions.<T, P>map(promise, promiseFn);
    Promise<R> _flatten = PromiseExtensions.<R, P>flatten(_map);
    final Procedure1<Promise<R>> _function = new Procedure1<Promise<R>>() {
      public void apply(final Promise<R> it) {
        it.setOperation("flatMap");
      }
    };
    return ObjectExtensions.<Promise<R>>operator_doubleArrow(_flatten, _function);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<R> flatMap(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends P> promiseFn) {
    Promise<P> _map = PromiseExtensions.<K, T, P>map(promise, promiseFn);
    Promise<R> _flatten = PromiseExtensions.<R, P>flatten(_map);
    final Procedure1<Promise<R>> _function = new Procedure1<Promise<R>>() {
      public void apply(final Promise<R> it) {
        it.setOperation("flatMap");
      }
    };
    return ObjectExtensions.<Promise<R>>operator_doubleArrow(_flatten, _function);
  }
  
  /**
   * Perform some side-effect action based on the promise. It will not
   * really affect the promise itself.
   */
  public static <T extends Object> Promise<T> effect(final IPromise<T> promise, final Procedure1<? super T> listener) {
    final Function1<T, T> _function = new Function1<T, T>() {
      public T apply(final T it) {
        T _xblockexpression = null;
        {
          listener.apply(it);
          _xblockexpression = it;
        }
        return _xblockexpression;
      }
    };
    Promise<T> _map = PromiseExtensions.<T, T>map(promise, _function);
    final Procedure1<Promise<T>> _function_1 = new Procedure1<Promise<T>>() {
      public void apply(final Promise<T> it) {
        it.setOperation("effect");
      }
    };
    return ObjectExtensions.<Promise<T>>operator_doubleArrow(_map, _function_1);
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
  public static <T extends Object, R extends Object, P extends IPromise<R>> IPromise<R> call(final IPromise<T> promise, final Function1<? super T, ? extends P> promiseFn) {
    Promise<P> _map = PromiseExtensions.<T, P>map(promise, promiseFn);
    Promise<R> _resolve = PromiseExtensions.<R, P>resolve(_map);
    final Procedure1<Promise<R>> _function = new Procedure1<Promise<R>>() {
      public void apply(final Promise<R> it) {
        it.setOperation("call");
      }
    };
    return ObjectExtensions.<Promise<R>>operator_doubleArrow(_resolve, _function);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<R> call(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends P> promiseFn) {
    Promise<P> _map = PromiseExtensions.<K, T, P>map(promise, promiseFn);
    Promise<R> _resolve = PromiseExtensions.<R, P>resolve(_map);
    final Procedure1<Promise<R>> _function = new Procedure1<Promise<R>>() {
      public void apply(final Promise<R> it) {
        it.setOperation("call");
      }
    };
    return ObjectExtensions.<Promise<R>>operator_doubleArrow(_resolve, _function);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>, K2 extends Object> IPromise<Pair<K, R>> call2(final IPromise<Pair<K, T>> promise, final Function2<? super K, ? super T, ? extends Pair<K, P>> promiseFn) {
    Promise<Pair<K, P>> _map = PromiseExtensions.<K, T, Pair<K, P>>map(promise, promiseFn);
    Promise<Pair<K, R>> _resolveValue = PromiseExtensions.<K, R, P>resolveValue(_map);
    final Procedure1<Promise<Pair<K, R>>> _function = new Procedure1<Promise<Pair<K, R>>>() {
      public void apply(final Promise<Pair<K, R>> it) {
        it.setOperation("call2");
      }
    };
    return ObjectExtensions.<Promise<Pair<K, R>>>operator_doubleArrow(_resolveValue, _function);
  }
  
  public static <T extends Object, R extends Object, K extends Object, P extends IPromise<R>> IPromise<Pair<K, R>> call2(final IPromise<T> promise, final Function1<? super T, ? extends Pair<K, P>> promiseFn) {
    Promise<Pair<K, P>> _map = PromiseExtensions.<T, Pair<K, P>>map(promise, promiseFn);
    Promise<Pair<K, R>> _resolveValue = PromiseExtensions.<K, R, P>resolveValue(_map);
    final Procedure1<Promise<Pair<K, R>>> _function = new Procedure1<Promise<Pair<K, R>>>() {
      public void apply(final Promise<Pair<K, R>> it) {
        it.setOperation("call2");
      }
    };
    return ObjectExtensions.<Promise<Pair<K, R>>>operator_doubleArrow(_resolveValue, _function);
  }
  
  public static <T extends Object> IPromise<T> onError(final IPromise<T> promise, final Procedure2<? super Throwable, ? super T> listener) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable t) {
        boolean _matched = false;
        if (!_matched) {
          if (t instanceof PromiseException) {
            boolean _notEquals = (!Objects.equal(((PromiseException)t).value, null));
            if (_notEquals) {
              _matched=true;
              listener.apply(t, ((T) ((PromiseException)t).value));
            }
          }
        }
        if (!_matched) {
          listener.apply(t, null);
        }
      }
    };
    return promise.onError(_function);
  }
  
  /**
   * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
   * See chain2() for example of how to use.
   */
  public static <K extends Object, V extends Object> Task then(final IPromise<Pair<K, V>> promise, final Procedure2<? super K, ? super V> listener) {
    final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
      public void apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value);
      }
    };
    return promise.then(_function);
  }
  
  /**
   * Convert or forward a promise to a task
   */
  public static Task asTask(final IPromise<?> promise) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      PromiseExtensions.completes(promise, task);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <T extends Object> IPromise<T> pipe(final IPromise<T> promise, final IPromise<T> target) {
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        target.apply(it);
      }
    };
    return PromiseExtensions.<T>always(promise, _function);
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <T extends Object> Task completes(final IPromise<T> promise, final Task task) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        task.error(it);
      }
    };
    IPromise<T> _onError = promise.onError(_function);
    final Procedure1<T> _function_1 = new Procedure1<T>() {
      public void apply(final T it) {
        task.complete();
      }
    };
    return _onError.then(_function_1);
  }
}
