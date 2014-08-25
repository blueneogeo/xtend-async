package nl.kii.promise;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.stream.Entry;
import nl.kii.stream.Value;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
@SuppressWarnings("all")
public class Promise<T extends Object> implements IPromise<T> {
  private final Publisher<Entry<T>> publisher = new Publisher<Entry<T>>();
  
  /**
   * Property to see if the promise is fulfulled
   */
  @Atomic
  private final AtomicBoolean _fulfilled = new AtomicBoolean(false);
  
  /**
   * The result of the promise, if any, otherwise null
   */
  @Atomic
  private final AtomicReference<Entry<T>> _entry = new AtomicReference<Entry<T>>();
  
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
  public Promise<T> set(final T value) {
    Promise<T> _xblockexpression = null;
    {
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        throw new NullPointerException("cannot promise a null value");
      }
      Value<T> _value = new Value<T>(value);
      this.apply(_value);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * report an error to the listener of the promise.
   */
  public Promise<T> error(final Throwable t) {
    Promise<T> _xblockexpression = null;
    {
      nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(t);
      this.apply(_error);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public void apply(final Entry<T> it) {
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
   * If the promise recieved or recieves an error, onError is called with the throwable
   */
  public Promise<T> onError(final Procedure1<Throwable> errorFn) {
    Promise<T> _xblockexpression = null;
    {
      final AtomicReference<Procedure0> sub = new AtomicReference<Procedure0>();
      final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
        public void apply(final Entry<T> it) {
          boolean _matched = false;
          if (!_matched) {
            if (it instanceof nl.kii.stream.Error) {
              _matched=true;
              Procedure0 _get = sub.get();
              _get.apply();
              errorFn.apply(((nl.kii.stream.Error<T>)it).error);
            }
          }
        }
      };
      Procedure0 _onChange = this.publisher.onChange(_function);
      sub.set(_onChange);
      Entry<T> _entry = this.getEntry();
      boolean _notEquals = (!Objects.equal(_entry, null));
      if (_notEquals) {
        Entry<T> _entry_1 = this.getEntry();
        this.publisher.apply(_entry_1);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Call the passed onValue procedure when the promise has been fulfilled with value. This also starts the onError and always listening.
   */
  public Promise<T> then(final Procedure1<T> valueFn) {
    Promise<T> _xblockexpression = null;
    {
      final AtomicReference<Procedure0> sub = new AtomicReference<Procedure0>();
      final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
        public void apply(final Entry<T> it) {
          try {
            boolean _matched = false;
            if (!_matched) {
              if (it instanceof Value) {
                _matched=true;
                Procedure0 _get = sub.get();
                _get.apply();
                valueFn.apply(((Value<T>)it).value);
              }
            }
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception e = (Exception)_t;
              Promise.this.error(e);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        }
      };
      Procedure0 _onChange = this.publisher.onChange(_function);
      sub.set(_onChange);
      Entry<T> _entry = this.getEntry();
      boolean _notEquals = (!Objects.equal(_entry, null));
      if (_notEquals) {
        Entry<T> _entry_1 = this.getEntry();
        this.publisher.apply(_entry_1);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
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
  
  protected Entry<T> setEntry(final Entry<T> value) {
    return this._entry.getAndSet(value);
  }
  
  protected Entry<T> getEntry() {
    return this._entry.get();
  }
}
