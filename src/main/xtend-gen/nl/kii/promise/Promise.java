package nl.kii.promise;

import com.google.common.base.Objects;
import nl.kii.promise.BasePromise;
import nl.kii.promise.IPromise;
import nl.kii.stream.Value;

@SuppressWarnings("all")
public class Promise<T extends Object> extends BasePromise<T, T> {
  public IPromise<T, ?> getRoot() {
    return this;
  }
  
  public Promise() {
  }
  
  public Promise(final T t) {
    this.set(t);
  }
  
  /**
   * set the promised value
   */
  public void set(final T value) {
    boolean _equals = Objects.equal(value, null);
    if (_equals) {
      throw new NullPointerException("cannot promise a null value");
    }
    Value<T, T> _value = new Value<T, T>(value, value);
    this.apply(_value);
  }
  
  /**
   * report an error to the listener of the promise.
   */
  public IPromise<T, T> error(final Throwable t) {
    Promise<T> _xblockexpression = null;
    {
      nl.kii.stream.Error<T, T> _error = new nl.kii.stream.Error<T, T>(null, t);
      this.apply(_error);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
}
