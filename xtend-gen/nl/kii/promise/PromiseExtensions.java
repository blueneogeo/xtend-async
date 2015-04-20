package nl.kii.promise;

import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import nl.kii.stream.Stream;
import nl.kii.stream.message.Entry;

public class PromiseExtensions {
  /**
   * Create a promise of the given type
   */
  public static <T extends java.lang.Object> Promise<T> promise(final /* Class<T> */Object type) {
    return new Promise<T>();
  }
  
  /**
   * Create a promise of a list of the given type
   */
  public static <T extends java.lang.Object> /* Promise<List> */Object promiseList(final /* Class<T> */Object type) {
    throw new Error("Unresolved compilation problems:"
      + "\nList cannot be resolved to a type.");
  }
  
  /**
   * Create a promise of a map of the given key and value types
   */
  public static <K extends java.lang.Object, V extends java.lang.Object> /* Promise<Map> */Object promiseMap(final /* Pair<Class<K>, Class<V>> */Object type) {
    throw new Error("Unresolved compilation problems:"
      + "\nMap cannot be resolved to a type.");
  }
  
  /**
   * Create a promise that immediately resolves to the passed value.
   */
  public static <T extends java.lang.Object> Promise<T> promise(final T value) {
    return new Promise<T>(value);
  }
  
  public static <I extends java.lang.Object, O extends java.lang.Object> Object promise(final I from, final O value) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\nThe method set is undefined for the type PromiseExtensions");
  }
  
  /**
   * Create a promise of a pair
   */
  public static <K extends java.lang.Object, V extends java.lang.Object> /* Promise<Pair> */Object promisePair(final /* Pair<Class<K>, Class<V>> */Object type) {
    throw new Error("Unresolved compilation problems:"
      + "\nPair cannot be resolved to a type.");
  }
  
  /**
   * Distribute work using an asynchronous method
   */
  public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object, P extends IPromise<I2, O>> /* IPromise<I, List<O>> */Object call(final /* List<I> */Object data, final int concurrency, final /*  */Object operationFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\ncollect cannot be resolved"
      + "\nfirst cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Shortcut for quickly creating a completed task
   */
  public static Task complete() {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved.");
  }
  
  /**
   * Shortcut for quickly creating a promise with an error
   */
  public static Task error(final /* String */Object message) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\nerror cannot be resolved");
  }
  
  /**
   * Shortcut for quickly creating a promise with an error
   */
  public static <T extends java.lang.Object> Promise<T> error(final /* Class<T> */Object cls, final /* String */Object message) {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\nerror cannot be resolved");
  }
  
  /**
   * Create a new Task that completes when all wrapped tasks are completed.
   * Errors created by the tasks are propagated into the resulting task.
   */
  public static Task all(final /* IPromise<? extends  */Object... promises) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method toList is undefined for the type PromiseExtensions");
  }
  
  /**
   * Create a new Task that completes when all wrapped tasks are completed.
   * Errors created by the tasks are propagated into the resulting task.
   */
  public static Task all(final /* Iterable<? extends IPromise<? extends  */Object promises) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of arguments. The method asTask(IPromise<I, O>) is not applicable without arguments"
      + "\nmap cannot be resolved"
      + "\nstream cannot be resolved"
      + "\ncall cannot be resolved"
      + "\ncollect cannot be resolved"
      + "\nfirst cannot be resolved"
      + "\nasTask cannot be resolved");
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, P extends IPromise<I, O>> Task any(final P... promises) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method toList is undefined for the type PromiseExtensions");
  }
  
  /**
   * Create a new Task that completes when any of the wrapped tasks are completed
   * Errors created by the promises are propagated into the resulting task
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Task any(final /* List<? extends IPromise<I, O>> */Object promises) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\non cannot be resolved"
      + "\nthen cannot be resolved");
  }
  
  /**
   * Always call onResult, whether the promise has been either fulfilled or had an error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> always(final IPromise<I, O> promise, final /* Procedures.Procedure1<Entry<? extends  */Object resultFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\napply cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Tell the promise it went wrong
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> error(final IPromise<I, O> promise, final /* String */Object message) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved.");
  }
  
  /**
   * Tell the promise it went wrong, with the cause throwable
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> error(final IPromise<I, O> promise, final /* String */Object message, final /* Throwable */Object cause) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved.");
  }
  
  /**
   * Fulfill a promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> operator_doubleGreaterThan(final I value, final IPromise<I, O> promise) {
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
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> operator_doubleLessThan(final IPromise<I, O> promise, final I value) {
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
  public static Task operator_and(final /* IPromise<? extends  */Object p1, final /* IPromise<? extends  */Object p2) {
    return PromiseExtensions.all(p1, p2);
  }
  
  /**
   * Any/Or
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Task operator_or(final IPromise<I, O> p1, final IPromise<I, O> p2) {
    return PromiseExtensions.<java.lang.Object, java.lang.Object, IPromise<I, O>>any(p1, p2);
  }
  
  /**
   * Transform the value of a promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> java.lang.Object map(final IPromise<I, O> promise, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Transform the value of a promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object> Object map(final IPromise<I, O> promise, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method on is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved");
  }
  
  /**
   * Asynchronously transform the value of a promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<?, R>> Object call(final IPromise<I, O> promise, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nresolve cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Asynchronously transform the value of a promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<?, R>> Object call(final IPromise<I, O> promise, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nresolve cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Perform some side-effect action based on the promise.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object effect(final IPromise<I, O> promise, final /*  */Object listener) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Perform some side-effect action based on the promise.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object effect(final IPromise<I, O> promise, final /*  */Object listener) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\napply cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Asynchronously perform some side effect based on the result of the promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object perform(final IPromise<I, O> promise, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Asynchronously perform some side effect based on the result of the promise
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object perform(final IPromise<I, O> promise, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\napply cannot be resolved"
      + "\nmap cannot be resolved"
      + "\nresolve cannot be resolved"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Listen for an error coming from the promise. Does not swallow the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> on(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Listen for an error coming from the promise. Does not swallow the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> on(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    return promise.on(errorType, false, handler);
  }
  
  /**
   * Listen for an error coming from the promise. Swallows the the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> effect(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Listen for an error coming from the promise. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> effect(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object handler) {
    return promise.on(errorType, true, handler);
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object map(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object map(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved to a type."
      + "\nThe method fulfilled is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Exception)"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nNo exception of type Exception can be thrown; an exception type must be a subclass of Throwable"
      + "\n! cannot be resolved"
      + "\napply cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nsimpleName cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object call(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Object call(final IPromise<I, O> promise, final /* Class<? extends Throwable> */Object errorType, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved to a type."
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Exception)"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nType mismatch: cannot convert from Promise<I> to IPromise<I, ? extends Object>"
      + "\nNo exception of type Exception can be thrown; an exception type must be a subclass of Throwable"
      + "\napply cannot be resolved"
      + "\non cannot be resolved"
      + "\nthen cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nsimpleName cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> java.lang.Object onErrorMap(final IPromise<I, O> promise, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object> java.lang.Object onErrorCall(final IPromise<I, O> promise, final /*  */Object mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions");
  }
  
  /**
   * Create a new promise with a new input, defined by the inputFn
   */
  public static <I1 extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object> Object mapInput(final IPromise<I1, O> promise, final /*  */Object inputFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nType mismatch: cannot convert from Promise<I2> to I2"
      + "\napply cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Create a stream out of a promise of a stream.
   */
  public static <I extends java.lang.Object, P extends IPromise<I, Stream<T>>, T extends java.lang.Object> Stream<T> toStream(final P promise) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\npipe cannot be resolved");
  }
  
  /**
   * Resolve a promise of a promise to directly a promise.
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, P extends IPromise<?, O>> Object resolve(final IPromise<I, P> promise) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved."
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)"
      + "\nthen cannot be resolved"
      + "\non cannot be resolved");
  }
  
  /**
   * Flattens a promise of a promise to directly a promise. Alias of .resolve
   */
  public static <I1 extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object, P extends IPromise<I1, O>> Object flatten(final IPromise<I2, P> promise) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved");
  }
  
  /**
   * Performs a flatmap, which is a combination of map and flatten/resolve. Alias of .call
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, R extends java.lang.Object, P extends IPromise<I, R>> IPromise<I, R> flatMap(final IPromise<I, O> promise, final /*  */Object promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field operation is undefined for the type PromiseExtensions"
      + "\n=> cannot be resolved");
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
  public static <I extends java.lang.Object, O extends java.lang.Object> SubPromise<I, O> wait(final IPromise<I, O> promise, final long periodMs, final /* Procedure2<Long, Procedure0> */Object timerFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\nInvalid number of arguments. The method set(I) is not applicable for the arguments (Object,Object)"
      + "\napply cannot be resolved");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> onErrorThrow(final IPromise<I, O> promise, final /*  */Object exceptionFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\napply cannot be resolved");
  }
  
  /* @Deprecated
   */public static <I extends java.lang.Object, O extends java.lang.Object> IPromise<I, O> onErrorThrow(final IPromise<I, O> promise, final /* String */Object message) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\nException cannot be resolved."
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * Convert or forward a promise to a task
   */
  public static <I extends java.lang.Object, O extends java.lang.Object> Task asTask(final IPromise<I, O> promise) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      PromiseExtensions.<I, java.lang.Object, O>completes(promise, task);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <I extends java.lang.Object, O extends java.lang.Object, O2 extends java.lang.Object> Task pipe(final IPromise<I, O> promise, final IPromise<O, O2> target) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions");
  }
  
  /**
   * Forward the events from this promise to another promise of the same type
   */
  public static <I extends java.lang.Object, I2 extends java.lang.Object, O extends java.lang.Object> Object completes(final IPromise<I, O> promise, final Task task) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method on is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions"
      + "\nThe method or field Throwable is undefined for the type PromiseExtensions");
  }
}
