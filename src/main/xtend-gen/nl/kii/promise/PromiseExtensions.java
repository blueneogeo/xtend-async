package nl.kii.promise;

import java.util.List;
import java.util.Map;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.internal.SubStream;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
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
  
  public static <I extends Object, O extends Object> SubPromise<I, O> promise(final I from, final O value) {
    SubPromise<I, O> _subPromise = new SubPromise<I, O>();
    final Procedure1<SubPromise<I, O>> _function = (SubPromise<I, O> it) -> {
      it.set(from, value);
    };
    return ObjectExtensions.<SubPromise<I, O>>operator_doubleArrow(_subPromise, _function);
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
  public static <I extends Object, I2 extends Object, O extends Object, P extends IPromise<I2, O>> IPromise<I, List<O>> call(final List<I> data, final int concurrency, final Function1<? super I, ? extends P> operationFn) {
    Stream<I> _stream = StreamExtensions.<I>stream(data);
    SubStream<I, O> _call = StreamExtensions.<I, I, O, P>call(_stream, concurrency, operationFn);
    SubStream<I, List<O>> _collect = StreamExtensions.<I, O>collect(_call);
    IPromise<I, List<O>> _first = StreamExtensions.<I, List<O>>first(_collect);
    final Procedure1<IPromise<I, List<O>>> _function = (IPromise<I, List<O>> it) -> {
      it.setOperation((("call(concurrency=" + Integer.valueOf(concurrency)) + ")"));
    };
    return ObjectExtensions.<IPromise<I, List<O>>>operator_doubleArrow(_first, _function);
  }
  
  /**
   * Shortcut for quickly creating a completed task
   */
  public static Task complete() {
    Task _task = new Task();
    final Procedure1<Task> _function = (Task it) -> {
      it.complete();
    };
    return ObjectExtensions.<Task>operator_doubleArrow(_task, _function);
  }
  
  /**
   * Shortcut for quickly creating a promise with an error
   */
  public static Task error(final String message) {
    Task _task = new Task();
    final Procedure1<Task> _function = (Task it) -> {
      PromiseExtensions.<Boolean, Boolean>error(it, message);
    };
    return ObjectExtensions.<Task>operator_doubleArrow(_task, _function);
  }
  
  /**
   * Shortcut for quickly creating a promise with an error
   */
  public static <T extends Object> Promise<T> error(final Class<T> cls, final String message) {
    Promise<T> _promise = new Promise<T>();
    final Procedure1<Promise<T>> _function = (Promise<T> it) -> {
      PromiseExtensions.<T, T>error(it, message);
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
    final Function1<IPromise<?, ?>, Task> _function = (IPromise<?, ?> it) -> {
      return PromiseExtensions.asTask(it);
    };
    Iterable<Task> _map = IterableExtensions.map(promises, _function);
    Stream<Task> _stream = StreamExtensions.<Task>stream(_map);
    final Function1<Task, Task> _function_1 = (Task it) -> {
      return it;
    };
    SubStream<Task, Boolean> _call = StreamExtensions.<Task, Task, Boolean, Task>call(_stream, _function_1);
    SubStream<Task, List<Boolean>> _collect = StreamExtensions.<Task, Boolean>collect(_call);
    IPromise<Task, List<Boolean>> _first = StreamExtensions.<Task, List<Boolean>>first(_collect);
    return PromiseExtensions.<Task, List<Boolean>>asTask(_first);
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <I extends Object, O extends Object, P extends IPromise<I, O>> Task any(final P... promises) {
    List<P> _list = IterableExtensions.<P>toList(((Iterable<P>)Conversions.doWrapArray(promises)));
    return PromiseExtensions.<I, O>any(_list);
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <I extends Object, O extends Object> Task any(final List<? extends IPromise<I, O>> promises) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      for (final IPromise<I, O> promise : promises) {
        final Procedure1<Throwable> _function = (Throwable it) -> {
          task.error(it);
        };
        IPromise<I, O> _on = PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
        final Procedure1<O> _function_1 = (O it) -> {
          task.complete();
        };
        _on.then(_function_1);
      }
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Always call onResult, whether the promise has been either fulfilled or had an error.
   */
  public static <I extends Object, O extends Object> IPromise<I, O> always(final IPromise<I, O> promise, final Procedure1<Entry<?, O>> resultFn) {
    IPromise<I, O> _xblockexpression = null;
    {
      final Procedure1<Throwable> _function = (Throwable it) -> {
        nl.kii.stream.message.Error<Object, O> _error = new nl.kii.stream.message.Error<Object, O>(null, it);
        resultFn.apply(_error);
      };
      PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
      final Procedure1<O> _function_1 = (O it) -> {
        Value<Object, O> _value = new Value<Object, O>(null, it);
        resultFn.apply(_value);
      };
      promise.then(_function_1);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Tell the promise it went wrong
   */
  public static <I extends Object, O extends Object> IPromise<I, O> error(final IPromise<I, O> promise, final String message) {
    Exception _exception = new Exception(message);
    return promise.error(_exception);
  }
  
  /**
   * Tell the promise it went wrong, with the cause throwable
   */
  public static <I extends Object, O extends Object> IPromise<I, O> error(final IPromise<I, O> promise, final String message, final Throwable cause) {
    Exception _exception = new Exception(message, cause);
    return promise.error(_exception);
  }
  
  /**
   * Fulfill a promise
   */
  public static <I extends Object, O extends Object> IPromise<I, O> operator_doubleGreaterThan(final I value, final IPromise<I, O> promise) {
    IPromise<I, O> _xblockexpression = null;
    {
      promise.set(value);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Fulfill a promise
   */
  public static <I extends Object, O extends Object> IPromise<I, O> operator_doubleLessThan(final IPromise<I, O> promise, final I value) {
    IPromise<I, O> _xblockexpression = null;
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
  public static <I extends Object, O extends Object> Task operator_or(final IPromise<I, O> p1, final IPromise<I, O> p2) {
    return PromiseExtensions.<I, O, IPromise<I, O>>any(p1, p2);
  }
  
  /**
   * Transform the value of a promise
   */
  public static <I extends Object, O extends Object, R extends Object> SubPromise<I, R> map(final IPromise<I, O> promise, final Function1<? super O, ? extends R> mappingFn) {
    final Function2<I, O, R> _function = (I r, O it) -> {
      return mappingFn.apply(it);
    };
    return PromiseExtensions.<I, O, R>map(promise, _function);
  }
  
  /**
   * Transform the value of a promise
   */
  public static <I extends Object, O extends Object, R extends Object> SubPromise<I, R> map(final IPromise<I, O> promise, final Function2<? super I, ? super O, ? extends R> mappingFn) {
    SubPromise<I, R> _xblockexpression = null;
    {
      final SubPromise<I, R> newPromise = new SubPromise<I, R>(promise);
      final Procedure2<I, Throwable> _function = (I r, Throwable it) -> {
        newPromise.error(r, it);
      };
      IPromise<I, O> _on = PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
      final Procedure2<I, O> _function_1 = (I r, O it) -> {
        R _apply = mappingFn.apply(r, it);
        newPromise.set(r, _apply);
      };
      Task _then = _on.then(_function_1);
      final Procedure1<Throwable> _function_2 = (Throwable it) -> {
        newPromise.error(it);
      };
      PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_2);
      final Procedure1<SubPromise<I, R>> _function_3 = (SubPromise<I, R> it) -> {
        it.setOperation("map");
      };
      _xblockexpression = ObjectExtensions.<SubPromise<I, R>>operator_doubleArrow(newPromise, _function_3);
    }
    return _xblockexpression;
  }
  
  /**
   * Asynchronously transform the value of a promise
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<?, R>> SubPromise<I, R> call(final IPromise<I, O> promise, final Function1<? super O, ? extends P> promiseFn) {
    SubPromise<I, P> _map = PromiseExtensions.<I, O, P>map(promise, promiseFn);
    SubPromise<I, R> _resolve = PromiseExtensions.<I, R, P>resolve(_map);
    final Procedure1<SubPromise<I, R>> _function = (SubPromise<I, R> it) -> {
      it.setOperation("call");
    };
    return ObjectExtensions.<SubPromise<I, R>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Asynchronously transform the value of a promise
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<?, R>> SubPromise<I, R> call(final IPromise<I, O> promise, final Function2<? super I, ? super O, ? extends P> promiseFn) {
    SubPromise<I, P> _map = PromiseExtensions.<I, O, P>map(promise, promiseFn);
    SubPromise<I, R> _resolve = PromiseExtensions.<I, R, P>resolve(_map);
    final Procedure1<SubPromise<I, R>> _function = (SubPromise<I, R> it) -> {
      it.setOperation("call");
    };
    return ObjectExtensions.<SubPromise<I, R>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Perform some side-effect action based on the promise.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> effect(final IPromise<I, O> promise, final Procedure1<? super O> listener) {
    final Procedure2<I, O> _function = (I r, O it) -> {
      listener.apply(it);
    };
    return PromiseExtensions.<I, O>effect(promise, _function);
  }
  
  /**
   * Perform some side-effect action based on the promise.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> effect(final IPromise<I, O> promise, final Procedure2<? super I, ? super O> listener) {
    final Function2<I, O, O> _function = (I r, O it) -> {
      listener.apply(r, it);
      return it;
    };
    SubPromise<I, O> _map = PromiseExtensions.<I, O, O>map(promise, _function);
    final Procedure1<SubPromise<I, O>> _function_1 = (SubPromise<I, O> it) -> {
      it.setOperation("effect");
    };
    return ObjectExtensions.<SubPromise<I, O>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * Asynchronously perform some side effect based on the result of the promise
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> perform(final IPromise<I, O> promise, final Function1<? super O, ? extends IPromise<?, ?>> promiseFn) {
    final Function2<I, O, IPromise<?, ?>> _function = (I i, O o) -> {
      return promiseFn.apply(o);
    };
    return PromiseExtensions.<I, O>perform(promise, _function);
  }
  
  /**
   * Asynchronously perform some side effect based on the result of the promise
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> perform(final IPromise<I, O> promise, final Function2<? super I, ? super O, ? extends IPromise<?, ?>> promiseFn) {
    final Function2<I, O, SubPromise<?, O>> _function = (I i, O o) -> {
      IPromise<?, ?> _apply = promiseFn.apply(i, o);
      final Function1<Object, O> _function_1 = (Object it) -> {
        return o;
      };
      return PromiseExtensions.map(_apply, _function_1);
    };
    SubPromise<I, SubPromise<?, O>> _map = PromiseExtensions.<I, O, SubPromise<?, O>>map(promise, _function);
    SubPromise<I, O> _resolve = PromiseExtensions.<I, O, SubPromise<?, O>>resolve(_map);
    final Procedure1<SubPromise<I, O>> _function_1 = (SubPromise<I, O> it) -> {
      it.setOperation("perform");
    };
    return ObjectExtensions.<SubPromise<I, O>>operator_doubleArrow(_resolve, _function_1);
  }
  
  /**
   * Listen for an error coming from the promise. Does not swallow the error.
   */
  public static <I extends Object, O extends Object> IPromise<I, O> on(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Procedure1<? super Throwable> handler) {
    final Procedure2<I, Throwable> _function = (I from, Throwable it) -> {
      handler.apply(it);
    };
    return promise.on(errorType, false, _function);
  }
  
  /**
   * Listen for an error coming from the promise. Does not swallow the error.
   */
  public static <I extends Object, O extends Object> IPromise<I, O> on(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Procedure2<? super I, ? super Throwable> handler) {
    return promise.on(errorType, false, ((Procedure2<I, Throwable>)handler));
  }
  
  /**
   * Listen for an error coming from the promise. Swallows the the error.
   */
  public static <I extends Object, O extends Object> IPromise<I, O> effect(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Procedure1<? super Throwable> handler) {
    final Procedure2<I, Throwable> _function = (I from, Throwable it) -> {
      handler.apply(it);
    };
    return promise.on(errorType, true, _function);
  }
  
  /**
   * Listen for an error coming from the promise. Swallows the error.
   */
  public static <I extends Object, O extends Object> IPromise<I, O> effect(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Procedure2<? super I, ? super Throwable> handler) {
    return promise.on(errorType, true, ((Procedure2<I, Throwable>)handler));
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> map(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Function1<? super Throwable, ? extends O> mappingFn) {
    final Function2<I, Throwable, O> _function = (I from, Throwable it) -> {
      return mappingFn.apply(it);
    };
    return PromiseExtensions.<I, O>map(promise, errorType, _function);
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> map(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Function2<? super I, ? super Throwable, ? extends O> mappingFn) {
    SubPromise<I, O> _xblockexpression = null;
    {
      final SubPromise<I, O> newPromise = new SubPromise<I, O>(promise, false);
      final Procedure2<I, Throwable> _function = (I from, Throwable e) -> {
        try {
          Boolean _fulfilled = newPromise.getFulfilled();
          boolean _not = (!(_fulfilled).booleanValue());
          if (_not) {
            final O value = mappingFn.apply(from, e);
            newPromise.set(from, value);
          }
        } catch (final Throwable _t) {
          if (_t instanceof Exception) {
            final Exception e2 = (Exception)_t;
            newPromise.error(from, e2);
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      };
      IPromise<I, O> _on = promise.on(errorType, true, _function);
      final Procedure2<I, Throwable> _function_1 = (I from, Throwable e) -> {
        newPromise.error(from, e);
      };
      IPromise<I, O> _on_1 = PromiseExtensions.<I, O>on(_on, Throwable.class, _function_1);
      final Procedure2<I, O> _function_2 = (I from, O e) -> {
        newPromise.set(from, e);
      };
      _on_1.then(_function_2);
      final Procedure1<SubPromise<I, O>> _function_3 = (SubPromise<I, O> it) -> {
        String _simpleName = errorType.getSimpleName();
        String _plus = ("map(" + _simpleName);
        String _plus_1 = (_plus + ")");
        it.setOperation(_plus_1);
      };
      _xblockexpression = ObjectExtensions.<SubPromise<I, O>>operator_doubleArrow(newPromise, _function_3);
    }
    return _xblockexpression;
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> call(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Function1<? super Throwable, ? extends IPromise<?, O>> mappingFn) {
    final Function2<I, Throwable, IPromise<?, O>> _function = (I i, Throwable e) -> {
      return mappingFn.apply(e);
    };
    return PromiseExtensions.<I, O>call(promise, errorType, _function);
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> call(final IPromise<I, O> promise, final Class<? extends Throwable> errorType, final Function2<? super I, ? super Throwable, ? extends IPromise<?, O>> mappingFn) {
    SubPromise<I, O> _xblockexpression = null;
    {
      Promise<I> _promise = new Promise<I>();
      final SubPromise<I, O> newPromise = new SubPromise<I, O>(_promise, false);
      final Procedure2<I, Throwable> _function = (I from, Throwable e) -> {
        try {
          IPromise<?, O> _apply = mappingFn.apply(from, e);
          final Procedure1<Throwable> _function_1 = (Throwable it) -> {
            newPromise.error(from, it);
          };
          IPromise<?, O> _on = PromiseExtensions.on(_apply, Throwable.class, _function_1);
          final Procedure1<O> _function_2 = (O it) -> {
            newPromise.set(from, it);
          };
          _on.then(_function_2);
        } catch (final Throwable _t) {
          if (_t instanceof Exception) {
            final Exception e2 = (Exception)_t;
            newPromise.error(from, e2);
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      };
      IPromise<I, O> _on = promise.on(errorType, true, _function);
      final Procedure2<I, Throwable> _function_1 = (I from, Throwable e) -> {
        newPromise.error(from, e);
      };
      IPromise<I, O> _on_1 = PromiseExtensions.<I, O>on(_on, Throwable.class, _function_1);
      final Procedure2<I, O> _function_2 = (I from, O e) -> {
        newPromise.set(from, e);
      };
      _on_1.then(_function_2);
      final Procedure1<SubPromise<I, O>> _function_3 = (SubPromise<I, O> it) -> {
        String _simpleName = errorType.getSimpleName();
        String _plus = ("call(" + _simpleName);
        String _plus_1 = (_plus + ")");
        it.setOperation(_plus_1);
      };
      _xblockexpression = ObjectExtensions.<SubPromise<I, O>>operator_doubleArrow(newPromise, _function_3);
    }
    return _xblockexpression;
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> SubPromise<I, O> onErrorMap(final IPromise<I, O> promise, final Function1<? super Throwable, ? extends O> mappingFn) {
    return PromiseExtensions.<I, O>map(promise, Throwable.class, mappingFn);
  }
  
  @Deprecated
  public static <I extends Object, I2 extends Object, O extends Object> SubPromise<I, O> onErrorCall(final IPromise<I, O> promise, final Function1<? super Throwable, ? extends IPromise<I2, O>> mappingFn) {
    return PromiseExtensions.<I, O>call(promise, Throwable.class, mappingFn);
  }
  
  /**
   * Create a new promise with a new input, defined by the inputFn
   */
  public static <I1 extends Object, I2 extends Object, O extends Object> SubPromise<I2, O> mapInput(final IPromise<I1, O> promise, final Function2<? super I1, ? super O, ? extends I2> inputFn) {
    SubPromise<I2, O> _xblockexpression = null;
    {
      Promise<I2> _promise = new Promise<I2>();
      final SubPromise<I2, O> newPromise = new SubPromise<I2, O>(_promise);
      final Procedure2<I1, Throwable> _function = (I1 r, Throwable e) -> {
        I2 _apply = inputFn.apply(r, null);
        newPromise.error(_apply, e);
      };
      IPromise<I1, O> _on = PromiseExtensions.<I1, O>on(promise, Throwable.class, _function);
      final Procedure2<I1, O> _function_1 = (I1 r, O it) -> {
        I2 _apply = inputFn.apply(r, it);
        newPromise.set(_apply, it);
      };
      _on.then(_function_1);
      final Procedure1<SubPromise<I2, O>> _function_2 = (SubPromise<I2, O> it) -> {
        it.setOperation("input");
      };
      _xblockexpression = ObjectExtensions.<SubPromise<I2, O>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Create a stream out of a promise of a stream.
   */
  public static <I extends Object, P extends IPromise<I, Stream<T>>, T extends Object> Stream<T> toStream(final P promise) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Throwable> _function = (Throwable it) -> {
        newStream.error(it);
      };
      IPromise<I, Stream<T>> _on = PromiseExtensions.<I, Stream<T>>on(promise, Throwable.class, _function);
      final Procedure1<Stream<T>> _function_1 = (Stream<T> s) -> {
        StreamExtensions.<T, T>pipe(s, newStream);
      };
      _on.then(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Resolve a promise of a promise to directly a promise.
   */
  public static <I extends Object, O extends Object, P extends IPromise<?, O>> SubPromise<I, O> resolve(final IPromise<I, P> promise) {
    SubPromise<I, O> _xblockexpression = null;
    {
      final SubPromise<I, O> newPromise = new SubPromise<I, O>(promise);
      final Procedure2<I, Throwable> _function = (I r, Throwable e) -> {
        newPromise.error(r, e);
      };
      IPromise<I, P> _on = PromiseExtensions.<I, P>on(promise, Throwable.class, _function);
      final Procedure2<I, P> _function_1 = (I r, P p) -> {
        final Procedure1<O> _function_2 = (O it) -> {
          newPromise.set(r, it);
        };
        Task _then = p.then(_function_2);
        final Procedure1<Throwable> _function_3 = (Throwable it) -> {
          newPromise.error(r, it);
        };
        PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_3);
      };
      _on.then(_function_1);
      final Procedure1<SubPromise<I, O>> _function_2 = (SubPromise<I, O> it) -> {
        it.setOperation("resolve");
      };
      _xblockexpression = ObjectExtensions.<SubPromise<I, O>>operator_doubleArrow(newPromise, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Flattens a promise of a promise to directly a promise. Alias of .resolve
   */
  public static <I1 extends Object, I2 extends Object, O extends Object, P extends IPromise<I1, O>> SubPromise<I2, O> flatten(final IPromise<I2, P> promise) {
    SubPromise<I2, O> _resolve = PromiseExtensions.<I2, O, P>resolve(promise);
    final Procedure1<SubPromise<I2, O>> _function = (SubPromise<I2, O> it) -> {
      it.setOperation("flatten");
    };
    return ObjectExtensions.<SubPromise<I2, O>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Performs a flatmap, which is a combination of map and flatten/resolve. Alias of .call
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<I, R>> IPromise<I, R> flatMap(final IPromise<I, O> promise, final Function1<? super O, ? extends P> promiseFn) {
    SubPromise<I, R> _call = PromiseExtensions.<I, O, R, P>call(promise, promiseFn);
    final Procedure1<SubPromise<I, R>> _function = (SubPromise<I, R> it) -> {
      it.setOperation("flatMap");
    };
    return ObjectExtensions.<SubPromise<I, R>>operator_doubleArrow(_call, _function);
  }
  
  /**
   * Create a new promise that delays the output (not the error) of the existing promise.
   * The idea here is that since timing has to be executed on another thread, instead of
   * having a threaded implementation, this method requires you to call your own implementation.
   * To do so, you implement the timerFn, and then pass it.
   * <p>
   * Example:
   * <pre>
   * val exec = Executors.newSingleThreadScheduledExecutor
   * val timerFn = [ long delayMs, =>void fn | exec.schedule(fn, delayMs, TimeUnit.MILLISECONDS) return ]
   * complete.wait(100, timerFn).then [ anyDone = true ]
   * </pre>
   * @param timerFn a function with two parameters: a delay in milliseconds, and a closure
   * 			calling the function should execute the closure after the delay.
   */
  public static <I extends Object, O extends Object> SubPromise<I, O> wait(final IPromise<I, O> promise, final long periodMs, final Procedure2<Long, Procedure0> timerFn) {
    SubPromise<I, O> _xblockexpression = null;
    {
      final SubPromise<I, O> newPromise = new SubPromise<I, O>(promise);
      final Procedure1<Throwable> _function = (Throwable it) -> {
        newPromise.error(it);
      };
      IPromise<I, O> _on = PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
      final Procedure2<I, O> _function_1 = (I input, O value) -> {
        final Procedure0 _function_2 = () -> {
          newPromise.set(input, value);
        };
        timerFn.apply(Long.valueOf(periodMs), _function_2);
      };
      _on.then(_function_1);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> IPromise<I, O> onErrorThrow(final IPromise<I, O> promise, final Function2<? super I, ? super Throwable, ? extends Exception> exceptionFn) {
    final Procedure2<I, Throwable> _function = (I i, Throwable t) -> {
      try {
        throw exceptionFn.apply(i, t);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    return PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> IPromise<I, O> onErrorThrow(final IPromise<I, O> promise, final String message) {
    final Procedure2<I, Throwable> _function = (I i, Throwable t) -> {
      try {
        throw new Exception(((message + ", for input ") + i), t);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    };
    return PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
  }
  
  /**
   * Convert or forward a promise to a task
   */
  public static <I extends Object, O extends Object> Task asTask(final IPromise<I, O> promise) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      PromiseExtensions.<I, Object, O>completes(promise, task);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <I extends Object, O extends Object, O2 extends Object> Task pipe(final IPromise<I, O> promise, final IPromise<O, O2> target) {
    final Procedure1<Throwable> _function = (Throwable it) -> {
      target.error(it);
    };
    IPromise<I, O> _on = PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
    final Procedure1<O> _function_1 = (O it) -> {
      target.set(it);
    };
    return _on.then(_function_1);
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <I extends Object, I2 extends Object, O extends Object> IPromise<Boolean, Boolean> completes(final IPromise<I, O> promise, final Task task) {
    final Procedure2<I, Throwable> _function = (I r, Throwable it) -> {
      task.error(it);
    };
    IPromise<I, O> _on = PromiseExtensions.<I, O>on(promise, Throwable.class, _function);
    final Procedure2<I, O> _function_1 = (I r, O it) -> {
      task.set(Boolean.valueOf(true));
    };
    Task _then = _on.then(_function_1);
    final Procedure2<Boolean, Throwable> _function_2 = (Boolean r, Throwable it) -> {
      task.error(it);
    };
    return PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_2);
  }
}
