package nl.kii.promise;

import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseException;
import nl.kii.promise.SubPromise;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SubStream;
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
  public static <T extends Object> Promise<T> error(final String message) {
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
  public static Task all(final IPromise<?, ?>... promises) {
    List<IPromise<?, ?>> _list = IterableExtensions.<IPromise<?, ?>>toList(((Iterable<IPromise<?, ?>>)Conversions.doWrapArray(promises)));
    return PromiseExtensions.all(_list);
  }
  
  /**
   * Create a new Task that completes when all wrapped tasks are completed.
   * Errors created by the tasks are propagated into the resulting task.
   */
  public static Task all(final Iterable<? extends IPromise<?, ?>> promises) {
    final Function1<IPromise<?, ?>, Task> _function = new Function1<IPromise<?, ?>, Task>() {
      public Task apply(final IPromise<?, ?> it) {
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
    SubStream<Task, Boolean> _call = StreamExtensions.<Task, Boolean, Task, Boolean, Task>call(_stream, _function_1);
    SubStream<Task, List<Boolean>> _collect = StreamExtensions.<Task, Boolean>collect(_call);
    Promise<List<Boolean>> _first = StreamExtensions.<Task, List<Boolean>>first(_collect);
    return PromiseExtensions.asTask(_first);
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <R extends Object, T extends Object, P extends IPromise<R, T>> Task any(final P... promises) {
    List<P> _list = IterableExtensions.<P>toList(((Iterable<P>)Conversions.doWrapArray(promises)));
    return PromiseExtensions.<R, T>any(_list);
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <R extends Object, T extends Object> Task any(final List<? extends IPromise<R, T>> promises) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      for (final IPromise<R, T> promise : promises) {
        final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            task.error(it);
          }
        };
        IPromise<R, T> _onError = promise.onError(_function);
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
  public static <R extends Object, T extends Object> IPromise<R, T> always(final IPromise<R, T> promise, final Procedure1<Entry<?, T>> resultFn) {
    IPromise<R, T> _xblockexpression = null;
    {
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          nl.kii.stream.Error<Object, T> _error = new nl.kii.stream.Error<Object, T>(null, it);
          resultFn.apply(_error);
        }
      };
      promise.onError(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          Value<Object, T> _value = new Value<Object, T>(null, it);
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
  public static <R extends Object, T extends Object> IPromise<R, T> error(final IPromise<R, T> promise, final String message) {
    Exception _exception = new Exception(message);
    return promise.error(_exception);
  }
  
  /**
   * Tell the promise it went wrong, with the cause throwable
   */
  public static <R extends Object, T extends Object> IPromise<R, T> error(final IPromise<R, T> promise, final String message, final Throwable cause) {
    Exception _exception = new Exception(message, cause);
    return promise.error(_exception);
  }
  
  /**
   * Fulfill a promise
   */
  public static <R extends Object, T extends Object> IPromise<R, T> operator_doubleGreaterThan(final R value, final IPromise<R, T> promise) {
    IPromise<R, T> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Fulfill a promise
   */
  public static <R extends Object, T extends Object> IPromise<R, T> operator_doubleLessThan(final IPromise<R, T> promise, final R value) {
    IPromise<R, T> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * All/And
   */
  public static Task operator_and(final IPromise<?, ?> p1, final IPromise<?, ?> p2) {
    return PromiseExtensions.all(p1, p2);
  }
  
  /**
   * Any/Or
   */
  public static <R extends Object, T extends Object> Task operator_or(final IPromise<R, T> p1, final IPromise<R, T> p2) {
    return PromiseExtensions.<R, T, IPromise<R, T>>any(p1, p2);
  }
  
  /**
   * Create a new promise from an existing promise,
   * that transforms the value of the promise
   * once the existing promise is resolved.
   */
  public static <R extends Object, T extends Object, M extends Object> SubPromise<R, M> map(final IPromise<R, T> promise, final Function1<? super T, ? extends M> mappingFn) {
    final Function2<R, T, M> _function = new Function2<R, T, M>() {
      public M apply(final R r, final T it) {
        return mappingFn.apply(it);
      }
    };
    return PromiseExtensions.<R, T, M>map(promise, _function);
  }
  
  /**
   * Create a new promise from an existing promise,
   * that transforms the value of the promise
   * once the existing promise is resolved.
   */
  public static <R extends Object, T extends Object, M extends Object> SubPromise<R, M> map(final IPromise<R, T> promise, final Function2<? super R, ? super T, ? extends M> mappingFn) {
    SubPromise<R, M> _xblockexpression = null;
    {
      final SubPromise<R, M> newPromise = new SubPromise<R, M>(promise);
      final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
        public void apply(final R r, final Throwable it) {
          newPromise.error(r, it);
        }
      };
      IPromise<R, T> _onError = promise.onError(_function);
      final Procedure2<R, T> _function_1 = new Procedure2<R, T>() {
        public void apply(final R r, final T it) {
          M _apply = mappingFn.apply(r, it);
          newPromise.set(r, _apply);
        }
      };
      _onError.then(_function_1);
      final Procedure1<SubPromise<R, M>> _function_2 = new Procedure1<SubPromise<R, M>>() {
        public void apply(final SubPromise<R, M> it) {
          it.setOperation("map");
        }
      };
      _xblockexpression = ObjectExtensions.<SubPromise<R, M>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Maps errors back into values.
   * Good for alternative path resolving and providing defaults.
   */
  public static <R extends Object, T extends Object> SubPromise<R, T> onErrorMap(final IPromise<R, T> promise, final Function1<? super Throwable, ? extends T> mappingFn) {
    SubPromise<R, T> _xblockexpression = null;
    {
      final SubPromise<R, T> newPromise = new SubPromise<R, T>(promise);
      final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
        public void apply(final R r, final Throwable it) {
          try {
            T _apply = mappingFn.apply(it);
            newPromise.set(r, _apply);
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
      IPromise<R, T> _onError = promise.onError(_function);
      final Procedure2<R, T> _function_1 = new Procedure2<R, T>() {
        public void apply(final R r, final T it) {
          newPromise.set(r, it);
        }
      };
      _onError.then(_function_1);
      final Procedure1<SubPromise<R, T>> _function_2 = new Procedure1<SubPromise<R, T>>() {
        public void apply(final SubPromise<R, T> it) {
          it.setOperation("onErrorMap");
        }
      };
      _xblockexpression = ObjectExtensions.<SubPromise<R, T>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Maps errors back into values.
   * Good for alternative path resolving and providing defaults.
   */
  public static <R extends Object, T extends Object> Promise<T> onErrorCall(final IPromise<R, T> promise, final Function1<? super Throwable, ? extends Promise<T>> mappingFn) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> newPromise = new Promise<T>();
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          try {
            Promise<T> _apply = mappingFn.apply(it);
            final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
              public void apply(final Throwable it) {
                newPromise.error(it);
              }
            };
            IPromise<T, T> _onError = _apply.onError(_function);
            final Procedure1<T> _function_1 = new Procedure1<T>() {
              public void apply(final T it) {
                newPromise.set(it);
              }
            };
            _onError.then(_function_1);
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
      IPromise<R, T> _onError = promise.onError(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          newPromise.set(it);
        }
      };
      _onError.then(_function_1);
      final Procedure1<Promise<T>> _function_2 = new Procedure1<Promise<T>>() {
        public void apply(final Promise<T> it) {
          it.setOperation("onErrorCall");
        }
      };
      _xblockexpression = ObjectExtensions.<Promise<T>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Flattens a promise of a promise to directly a promise.
   */
  public static <R1 extends Object, R2 extends Object, T extends Object, P extends IPromise<R1, T>> SubPromise<R2, T> flatten(final IPromise<R2, P> promise) {
    SubPromise<R2, T> _resolve = PromiseExtensions.<R2, R1, T, P>resolve(promise);
    final Procedure1<SubPromise<R2, T>> _function = new Procedure1<SubPromise<R2, T>>() {
      public void apply(final SubPromise<R2, T> it) {
        it.setOperation("flatten");
      }
    };
    return ObjectExtensions.<SubPromise<R2, T>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Create a stream out of a promise of a stream.
   */
  public static <R extends Object, P extends IPromise<R, Stream<T>>, T extends Object> Stream<T> toStream(final P promise) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          newStream.error(it);
        }
      };
      IPromise<R, Stream<T>> _onError = promise.onError(_function);
      final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
        public void apply(final Stream<T> s) {
          StreamExtensions.<T, T>pipe(s, newStream);
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
  public static <R extends Object, R2 extends Object, T extends Object, P extends IPromise<R2, T>> SubPromise<R, T> resolve(final IPromise<R, P> promise) {
    SubPromise<R, T> _xblockexpression = null;
    {
      final SubPromise<R, T> newPromise = new SubPromise<R, T>(promise);
      final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
        public void apply(final R r, final Throwable it) {
          newPromise.error(r, it);
        }
      };
      IPromise<R, P> _onError = promise.onError(_function);
      final Procedure2<R, P> _function_1 = new Procedure2<R, P>() {
        public void apply(final R r, final P p) {
          final Procedure2<R2, Throwable> _function = new Procedure2<R2, Throwable>() {
            public void apply(final R2 r2, final Throwable it) {
              newPromise.error(r, it);
            }
          };
          IPromise<R2, T> _onError = p.onError(_function);
          final Procedure2<R2, T> _function_1 = new Procedure2<R2, T>() {
            public void apply(final R2 r2, final T it) {
              newPromise.set(r, it);
            }
          };
          _onError.then(_function_1);
        }
      };
      _onError.then(_function_1);
      final Procedure1<SubPromise<R, T>> _function_2 = new Procedure1<SubPromise<R, T>>() {
        public void apply(final SubPromise<R, T> it) {
          it.setOperation("resolve");
        }
      };
      _xblockexpression = ObjectExtensions.<SubPromise<R, T>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Performs a flatmap, which is a combination of map and flatten/resolve
   */
  public static <R extends Object, T extends Object, M extends Object, P extends IPromise<R, M>> IPromise<R, M> flatMap(final IPromise<R, T> promise, final Function1<? super T, ? extends P> promiseFn) {
    SubPromise<R, P> _map = PromiseExtensions.<R, T, P>map(promise, promiseFn);
    SubPromise<R, M> _flatten = PromiseExtensions.<R, R, M, P>flatten(_map);
    final Procedure1<SubPromise<R, M>> _function = new Procedure1<SubPromise<R, M>>() {
      public void apply(final SubPromise<R, M> it) {
        it.setOperation("flatMap");
      }
    };
    return ObjectExtensions.<SubPromise<R, M>>operator_doubleArrow(_flatten, _function);
  }
  
  /**
   * Perform some side-effect action based on the promise. It should not affect
   * the promise itself however if an error is thrown, this is propagated to
   * the new generated promise.
   */
  public static <R extends Object, T extends Object> SubPromise<R, T> effect(final IPromise<R, T> promise, final Procedure1<? super T> listener) {
    final Procedure2<R, T> _function = new Procedure2<R, T>() {
      public void apply(final R r, final T it) {
        listener.apply(it);
      }
    };
    return PromiseExtensions.<R, T>effect(promise, _function);
  }
  
  /**
   * Perform some side-effect action based on the promise. It should not affect
   * the promise itself however if an error is thrown, this is propagated to
   * the new generated promise.
   */
  public static <R extends Object, T extends Object> SubPromise<R, T> effect(final IPromise<R, T> promise, final Procedure2<? super R, ? super T> listener) {
    final Function2<R, T, T> _function = new Function2<R, T, T>() {
      public T apply(final R r, final T it) {
        listener.apply(r, it);
        return it;
      }
    };
    SubPromise<R, T> _map = PromiseExtensions.<R, T, T>map(promise, _function);
    final Procedure1<SubPromise<R, T>> _function_1 = new Procedure1<SubPromise<R, T>>() {
      public void apply(final SubPromise<R, T> it) {
        it.setOperation("effect");
      }
    };
    return ObjectExtensions.<SubPromise<R, T>>operator_doubleArrow(_map, _function_1);
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
   *   .call [ checkCredentialsAsync ]
   *   .call [ signinUser ]
   *   .onError [ setErrorMessage('could not sign you in') ]
   *   .then [ println('success!') ]
   * </pre>
   */
  public static <R extends Object, R2 extends Object, T extends Object, M extends Object, P extends IPromise<R2, M>> SubPromise<R, M> call(final IPromise<R, T> promise, final Function1<? super T, ? extends P> promiseFn) {
    SubPromise<R, P> _map = PromiseExtensions.<R, T, P>map(promise, promiseFn);
    SubPromise<R, M> _resolve = PromiseExtensions.<R, R2, M, P>resolve(_map);
    final Procedure1<SubPromise<R, M>> _function = new Procedure1<SubPromise<R, M>>() {
      public void apply(final SubPromise<R, M> it) {
        it.setOperation("call");
      }
    };
    return ObjectExtensions.<SubPromise<R, M>>operator_doubleArrow(_resolve, _function);
  }
  
  public static <R extends Object, T extends Object> IPromise<R, T> onErrorThrow(final IPromise<R, T> promise, final Function2<? super Throwable, ? super T, ? extends Exception> exceptionFn) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable t) {
        try {
          Exception _switchResult = null;
          boolean _matched = false;
          if (!_matched) {
            if (t instanceof PromiseException) {
              boolean _notEquals = (!Objects.equal(((PromiseException)t).value, null));
              if (_notEquals) {
                _matched=true;
                _switchResult = exceptionFn.apply(t, ((T) ((PromiseException)t).value));
              }
            }
          }
          if (!_matched) {
            _switchResult = exceptionFn.apply(t, null);
          }
          throw _switchResult;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return promise.onError(_function);
  }
  
  /**
   * Convert or forward a promise to a task
   */
  public static Task asTask(final IPromise<?, ?> promise) {
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
  public static <T extends Object> IPromise<?, T> pipe(final IPromise<?, T> promise, final IPromise<T, ?> target) {
    final Procedure1<Entry<?, T>> _function = new Procedure1<Entry<?, T>>() {
      public void apply(final Entry<?, T> it) {
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Value) {
            _matched=true;
            target.set(((Value<?, T>)it).value);
          }
        }
        if (!_matched) {
          if (it instanceof nl.kii.stream.Error) {
            _matched=true;
            target.error(((nl.kii.stream.Error<?, T>)it).error);
          }
        }
      }
    };
    return PromiseExtensions.always(promise, _function);
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <R extends Object, T extends Object> Task completes(final IPromise<R, T> promise, final Task task) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        task.error(it);
      }
    };
    IPromise<R, T> _onError = promise.onError(_function);
    final Procedure1<T> _function_1 = new Procedure1<T>() {
      public void apply(final T it) {
        task.complete();
      }
    };
    return _onError.then(_function_1);
  }
}
