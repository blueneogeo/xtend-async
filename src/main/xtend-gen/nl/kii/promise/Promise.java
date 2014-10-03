package nl.kii.promise;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
@SuppressWarnings("all")
public class Promise<T extends Object> implements IPromise<T> {
  private final Publisher<Entry<T>> publisher /* Skipped initializer because of errors */;
  
  /**
   * Property to see if the promise is fulfulled
   */
  @Atomic
  private final AtomicBoolean _fulfilled = new AtomicBoolean(false);
  
  /**
   * Property to see if the promise has an error handler assigned
   */
  @Atomic
  private final AtomicBoolean _hasErrorHandler = new AtomicBoolean(false);
  
  /**
   * Property to see if the promise has a value handler assigned
   */
  @Atomic
  private final AtomicBoolean _hasValueHandler = new AtomicBoolean(false);
  
  /**
   * The result of the promise, if any, otherwise null
   */
  @Atomic
  private final AtomicReference<Entry<T>> _entry = new AtomicReference<Entry<T>>();
  
  /**
   * name of the operation the listener is performing
   */
  @Atomic
  private final AtomicReference<String> __operation = new AtomicReference<String>();
  
  /**
   * Create a new unfulfilled promise
   */
  public Promise() {
  }
  
  /**
   * Create a fulfilled promise
   */
  public Promise(final T value) {
    this.set(value);
  }
  
  /**
   * Constructor for easily creating a child promise
   */
  public Promise(final IPromise<?> parentPromise) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        Promise.this.error(it);
      }
    };
    parentPromise.onError(_function);
  }
  
  /**
   * only has a value when finished, otherwise null
   */
  public Entry<T> get() {
    return this.getEntry();
  }
  
  /**
   * set the promised value
   */
  public void set(final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of arguments. The constructor Value(R, T) is not applicable for the arguments (T)");
  }
  
  /**
   * report an error to the listener of the promise.
   */
  public IPromise<T> error(final Throwable t) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid number of arguments. The constructor Error(R, Throwable) is not applicable for the arguments (Throwable)"
      + "\nType mismatch: cannot convert from Throwable to T");
  }
  
  public void apply(final Entry<T> it) {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Error<R, T>; it cannot be parameterized with arguments <T>");
  }
  
  public Publisher<Entry<T>> getPublisher() {
    return this.publisher;
  }
  
  public String getOperation() {
    return this.get_operation();
  }
  
  public void setOperation(final String name) {
    this.set_operation(name);
  }
  
  /**
   * If the promise recieved or recieves an error, onError is called with the throwable.
   */
  public IPromise<T> onError(final Procedure1<Throwable> errorFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Error<R, T>; it cannot be parameterized with arguments <T>"
      + "\nIncorrect number of arguments for type Entry<R, T>; it cannot be parameterized with arguments <T>");
  }
  
  /**
   * Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening.
   */
  public Task then(final Procedure1<T> valueFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Value<R, T>; it cannot be parameterized with arguments <T>"
      + "\nIncorrect number of arguments for type Error<R, T>; it cannot be parameterized with arguments <T>"
      + "\nIncorrect number of arguments for type Entry<R, T>; it cannot be parameterized with arguments <T>");
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Promise { fulfilled: ");
    Boolean _fulfilled = this.getFulfilled();
    _builder.append(_fulfilled, "");
    _builder.append(", entry: ");
    Entry<T> _get = this.get();
    _builder.append(_get, "");
    _builder.append(" }");
    return _builder.toString();
  }
  
  public Boolean setFulfilled(final Boolean value) {
    return this._fulfilled.getAndSet(value);
  }
  
  public Boolean getFulfilled() {
    return this._fulfilled.get();
  }
  
  public Boolean setHasErrorHandler(final Boolean value) {
    return this._hasErrorHandler.getAndSet(value);
  }
  
  public Boolean getHasErrorHandler() {
    return this._hasErrorHandler.get();
  }
  
  public Boolean setHasValueHandler(final Boolean value) {
    return this._hasValueHandler.getAndSet(value);
  }
  
  public Boolean getHasValueHandler() {
    return this._hasValueHandler.get();
  }
  
  protected Entry<T> setEntry(final Entry<T> value) {
    return this._entry.getAndSet(value);
  }
  
  protected Entry<T> getEntry() {
    return this._entry.get();
  }
  
  private String set_operation(final String value) {
    return this.__operation.getAndSet(value);
  }
  
  private String get_operation() {
    return this.__operation.get();
  }
}
