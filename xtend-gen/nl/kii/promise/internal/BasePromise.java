package nl.kii.promise.internal;

import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Task;
import nl.kii.stream.message.Entry;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * Base implementation of IPromise.
 */
public abstract class BasePromise<I extends java.lang.Object, O extends java.lang.Object> implements IPromise<I, O> {
  private final Publisher<Entry<I, O>> publisher = new Publisher<Entry<I, O>>();
  
  /**
   * Property to see if the promise is fulfulled
   */
  /* @Atomic
   */public final boolean fulfilled = false;
  
  /**
   * Property to see if the promise has an error handler assigned
   */
  /* @Atomic
   */public final boolean hasErrorHandler = false;
  
  /**
   * Property to see if the promise has a value handler assigned
   */
  /* @Atomic
   */public final boolean hasValueHandler = false;
  
  /**
   * The result of the promise, if any, otherwise null
   */
  /* @Atomic
   */protected final Entry<I, O> entry;
  
  /**
   * name of the operation the listener is performing
   */
  /* @Atomic
   */private final /* String */Object _operation;
  
  public void apply(final Entry<I, O> it) {
    throw new Error("Unresolved compilation problems:"
      + "\n== cannot be resolved."
      + "\nNullPointerException cannot be resolved."
      + "\n! cannot be resolved."
      + "\n! cannot be resolved."
      + "\nAssignment to final field"
      + "\nAssignment to final field");
  }
  
  /**
   * only has a value when finished, otherwise null
   */
  public Entry<I, O> get() {
    return this.entry;
  }
  
  public Publisher<Entry<I, O>> getPublisher() {
    return this.publisher;
  }
  
  public String getOperation() {
    return this._operation;
  }
  
  public void setOperation(final /* String */Object name) {
    throw new Error("Unresolved compilation problems:"
      + "\nAssignment to final field");
  }
  
  /**
   * If the promise recieved or recieves an error, onError is called with the throwable.
   * Removes the error from the chain, so the returned promise no longer receives the error.
   * 
   * FIX: this method should return a subpromise with the error filtered out, but it returns this,
   * since there is a generics problem trying to assign the values.
   */
  public IPromise<I, O> on(final /* Class<? extends Throwable> */Object errorType, final boolean swallow, final /* Procedure2<I, Throwable> */Object errorFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved to a type."
      + "\nAtomicReference cannot be resolved."
      + "\nThe method class is undefined for the type BasePromise"
      + "\nThe method or field from is undefined for the type BasePromise"
      + "\n!= cannot be resolved."
      + "\nType mismatch: cannot convert from SubPromise<I, java.lang.Object> to IPromise<I, O>"
      + "\nProcedure0 cannot be resolved to a type."
      + "\nNo exception of type Exception can be thrown; an exception type must be a subclass of Throwable"
      + "\nAssignment to final field"
      + "\nset cannot be resolved"
      + "\nget cannot be resolved"
      + "\napply cannot be resolved"
      + "\nisAssignableFrom cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening.
   */
  public Task then(final /* Procedure1<O> */Object valueFn) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening.
   */
  public Task then(final /* Procedure2<I, O> */Object valueFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nException cannot be resolved to a type."
      + "\nAtomicReference cannot be resolved."
      + "\nThe method or field from is undefined for the type BasePromise"
      + "\nThe method or field value is undefined for the type BasePromise"
      + "\n!= cannot be resolved."
      + "\nProcedure0 cannot be resolved to a type."
      + "\nNo exception of type Exception can be thrown; an exception type must be a subclass of Throwable"
      + "\nAssignment to final field"
      + "\nset cannot be resolved"
      + "\nget cannot be resolved"
      + "\napply cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  public java.lang.CharSequence toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Promise { fulfilled: ");
    _builder.append(this.fulfilled, "");
    _builder.append(", entry: ");
    Entry<I, O> _get = this.get();
    _builder.append(_get, "");
    _builder.append(" }");
    return _builder;
  }
}
