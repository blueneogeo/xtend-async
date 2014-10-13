package nl.kii.promise;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.PromiseException;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Value;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
@SuppressWarnings("all")
public abstract class BasePromise<R extends Object, T extends Object> implements IPromise<R, T> {
  private final Publisher<Entry<R, T>> publisher = new Publisher<Entry<R, T>>();
  
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
  private final AtomicReference<Entry<R, T>> _entry = new AtomicReference<Entry<R, T>>();
  
  /**
   * name of the operation the listener is performing
   */
  @Atomic
  private final AtomicReference<String> __operation = new AtomicReference<String>();
  
  public void apply(final Entry<R, T> it) {
    boolean _equals = Objects.equal(it, null);
    if (_equals) {
      throw new NullPointerException("cannot promise a null entry");
    }
    boolean _switchResult = false;
    boolean _matched = false;
    if (!_matched) {
      Boolean _fulfilled = this.getFulfilled();
      boolean _not = (!(_fulfilled).booleanValue());
      if (_not) {
        _matched=true;
        _switchResult = true;
      }
    }
    if (!_matched) {
      if (it instanceof nl.kii.stream.Error) {
        Boolean _fulfilled_1 = this.getFulfilled();
        if (_fulfilled_1) {
          _matched=true;
          _switchResult = true;
        }
      }
    }
    if (!_matched) {
      _switchResult = false;
    }
    final boolean allowed = _switchResult;
    if ((!allowed)) {
      return;
    }
    this.setFulfilled(Boolean.valueOf(true));
    this.setEntry(it);
    this.publisher.apply(it);
  }
  
  /**
   * only has a value when finished, otherwise null
   */
  public Entry<R, T> get() {
    return this.getEntry();
  }
  
  public Publisher<Entry<R, T>> getPublisher() {
    return this.publisher;
  }
  
  public String getOperation() {
    return this.get_operation();
  }
  
  public void setOperation(final String name) {
    this.set_operation(name);
  }
  
  public IPromise<R, T> onError(final Procedure1<Throwable> errorFn) {
    final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
      public void apply(final R r, final Throwable t) {
        errorFn.apply(t);
      }
    };
    return this.onError(_function);
  }
  
  /**
   * If the promise recieved or recieves an error, onError is called with the throwable.
   * Removes the error from the chain, so the returned promise no longer receives the error.
   * 
   * FIX: this method should return a subpromise with the error filtered out, but it returns this,
   * since there is a generics problem trying to assign the values.
   */
  public IPromise<R, T> onError(final Procedure2<R, Throwable> errorFn) {
    BasePromise<R, T> _xblockexpression = null;
    {
      final AtomicReference<Procedure0> unregisterFn = new AtomicReference<Procedure0>();
      final Procedure1<Entry<R, T>> _function = new Procedure1<Entry<R, T>>() {
        public void apply(final Entry<R, T> it) {
          boolean _matched = false;
          if (!_matched) {
            if (it instanceof nl.kii.stream.Error) {
              _matched=true;
              Procedure0 _get = unregisterFn.get();
              _get.apply();
              errorFn.apply(((nl.kii.stream.Error<R, T>)it).from, ((nl.kii.stream.Error<R, T>)it).error);
            }
          }
        }
      };
      Procedure0 _onChange = this.publisher.onChange(_function);
      unregisterFn.set(_onChange);
      this.setHasErrorHandler(Boolean.valueOf(true));
      Entry<R, T> _entry = this.getEntry();
      boolean _notEquals = (!Objects.equal(_entry, null));
      if (_notEquals) {
        Entry<R, T> _entry_1 = this.getEntry();
        this.publisher.apply(_entry_1);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening.
   */
  public Task then(final Procedure1<T> valueFn) {
    final Procedure2<R, T> _function = new Procedure2<R, T>() {
      public void apply(final R r, final T it) {
        valueFn.apply(it);
      }
    };
    return this.then(_function);
  }
  
  /**
   * Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening.
   */
  public Task then(final Procedure2<R, T> valueFn) {
    Task _xblockexpression = null;
    {
      final Task newTask = new Task();
      final AtomicReference<Procedure0> unregisterFn = new AtomicReference<Procedure0>();
      final Procedure1<Entry<R, T>> _function = new Procedure1<Entry<R, T>>() {
        public void apply(final Entry<R, T> it) {
          try {
            boolean _matched = false;
            if (!_matched) {
              if (it instanceof Value) {
                _matched=true;
                Procedure0 _get = unregisterFn.get();
                _get.apply();
                valueFn.apply(((Value<R, T>)it).from, ((Value<R, T>)it).value);
                newTask.complete();
              }
            }
            if (!_matched) {
              if (it instanceof nl.kii.stream.Error) {
                _matched=true;
                newTask.error(((nl.kii.stream.Error<R, T>)it).error);
              }
            }
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              PromiseException _promiseException = new PromiseException("Promise.then gave error for", it, e);
              BasePromise.this.error(_promiseException);
              newTask.error(e);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      Procedure0 _onChange = this.publisher.onChange(_function);
      unregisterFn.set(_onChange);
      this.setHasValueHandler(Boolean.valueOf(true));
      Entry<R, T> _entry = this.getEntry();
      boolean _notEquals = (!Objects.equal(_entry, null));
      if (_notEquals) {
        Entry<R, T> _entry_1 = this.getEntry();
        this.publisher.apply(_entry_1);
      }
      _xblockexpression = newTask;
    }
    return _xblockexpression;
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Promise { fulfilled: ");
    Boolean _fulfilled = this.getFulfilled();
    _builder.append(_fulfilled, "");
    _builder.append(", entry: ");
    Entry<R, T> _get = this.get();
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
  
  protected Entry<R, T> setEntry(final Entry<R, T> value) {
    return this._entry.getAndSet(value);
  }
  
  protected Entry<R, T> getEntry() {
    return this._entry.get();
  }
  
  private String set_operation(final String value) {
    return this.__operation.getAndSet(value);
  }
  
  private String get_operation() {
    return this.__operation.get();
  }
}
