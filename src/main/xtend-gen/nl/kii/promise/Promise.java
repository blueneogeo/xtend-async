package nl.kii.promise;

import com.google.common.base.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.promise.PromiseException;
import nl.kii.stream.Entry;
import nl.kii.stream.Value;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A Promise is a publisher of a value. The value may arrive later.
 */
@SuppressWarnings("all")
public class Promise<T extends Object> implements Procedure1<Entry<T>> {
  private final AtomicReference<Entry<T>> _entry = new AtomicReference<Entry<T>>();
  
  private AtomicBoolean _fulfilled = new AtomicBoolean(false);
  
  /**
   * Lets others listen for the arrival of a value
   */
  private final AtomicReference<Procedure1<T>> _onValue = new AtomicReference<Procedure1<T>>();
  
  /**
   * Always called, both when there is a value and when there is an error
   */
  private final AtomicReference<Procedure1<Promise<T>>> _onResult = new AtomicReference<Procedure1<Promise<T>>>();
  
  /**
   * Lets others listen for errors occurring in the onValue listener
   */
  private final AtomicReference<Procedure1<Throwable>> _onError = new AtomicReference<Procedure1<Throwable>>();
  
  public Promise() {
  }
  
  public Promise(final T value) {
    this.set(value);
  }
  
  public Promise(final Promise<?> parentPromise) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        Promise.this.error(it);
      }
    };
    parentPromise.onError(_function);
  }
  
  public boolean isFulfilled() {
    return this._fulfilled.get();
  }
  
  /**
   * only has a value when finished, otherwise null
   */
  public Entry<T> get() {
    return this._entry.get();
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
    try {
      boolean _equals = Objects.equal(it, null);
      if (_equals) {
        throw new NullPointerException("cannot promise a null entry");
      }
      boolean _isFulfilled = this.isFulfilled();
      if (_isFulfilled) {
        throw new PromiseException(("cannot apply an entry to a completed promise. entry was: " + it));
      }
      this._fulfilled.set(true);
      Procedure1<T> _get = this._onValue.get();
      boolean _notEquals = (!Objects.equal(_get, null));
      if (_notEquals) {
        this.publish(it);
      } else {
        this._entry.set(it);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Promise<T> onError(final Procedure1<Throwable> onError) {
    Promise<T> _xblockexpression = null;
    {
      this._onError.set(onError);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public Promise<T> always(final Procedure1<Promise<T>> onResult) {
    try {
      Promise<T> _xblockexpression = null;
      {
        Procedure1<T> _get = this._onValue.get();
        boolean _notEquals = (!Objects.equal(_get, null));
        if (_notEquals) {
          throw new PromiseException("cannot listen to promise.always more than once");
        }
        this._onResult.set(onResult);
        _xblockexpression = this;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void then(final Procedure1<T> onValue) {
    try {
      Procedure1<T> _get = this._onValue.get();
      boolean _notEquals = (!Objects.equal(_get, null));
      if (_notEquals) {
        throw new PromiseException("cannot listen to promise.then more than once");
      }
      this._onValue.set(onValue);
      boolean _isFulfilled = this.isFulfilled();
      if (_isFulfilled) {
        Entry<T> _get_1 = this._entry.get();
        this.publish(_get_1);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected void buffer(final Entry<T> value) {
    Entry<T> _get = this._entry.get();
    boolean _equals = Objects.equal(_get, null);
    if (_equals) {
      this._entry.set(value);
    }
  }
  
  /**
   * Send an entry directly (no queue) to the listeners
   * (onValue, onError, onFinish). If a value was processed,
   * ready is set to false again, since the value was published.
   */
  protected void publish(final Entry<T> it) {
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Value) {
        _matched=true;
        Procedure1<Throwable> _get = this._onError.get();
        boolean _notEquals = (!Objects.equal(_get, null));
        if (_notEquals) {
          try {
            Procedure1<T> _get_1 = this._onValue.get();
            _get_1.apply(((Value<T>)it).value);
          } catch (final Throwable _t) {
            if (_t instanceof Throwable) {
              final Throwable t = (Throwable)_t;
              Procedure1<Throwable> _get_2 = this._onError.get();
              _get_2.apply(t);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        } else {
          Procedure1<T> _get_3 = this._onValue.get();
          _get_3.apply(((Value<T>)it).value);
        }
      }
    }
    if (!_matched) {
      if (it instanceof nl.kii.stream.Error) {
        _matched=true;
        Procedure1<Throwable> _get = this._onError.get();
        boolean _notEquals = (!Objects.equal(_get, null));
        if (_notEquals) {
          Procedure1<Throwable> _get_1 = this._onError.get();
          _get_1.apply(((nl.kii.stream.Error<T>)it).error);
        }
      }
    }
    Procedure1<Promise<T>> _get = this._onResult.get();
    boolean _notEquals = (!Objects.equal(_get, null));
    if (_notEquals) {
      try {
        Procedure1<Promise<T>> _get_1 = this._onResult.get();
        _get_1.apply(this);
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          Procedure1<Throwable> _get_2 = this._onError.get();
          _get_2.apply(t);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    }
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Promise { fulfilled: ");
    boolean _isFulfilled = this.isFulfilled();
    _builder.append(_isFulfilled, "");
    _builder.append(", entry: ");
    Entry<T> _get = this.get();
    _builder.append(_get, "");
    _builder.append(" }");
    return _builder.toString();
  }
}
