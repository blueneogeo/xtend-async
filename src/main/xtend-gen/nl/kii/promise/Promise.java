package nl.kii.promise;

import com.google.common.base.Objects;
import nl.kii.promise.IPromise;
import nl.kii.promise.internal.FixedBasePromise;
import nl.kii.stream.message.Value;

@SuppressWarnings("all")
public class Promise<T extends Object> extends FixedBasePromise<T, T> {
  @Override
  public IPromise<T, ?> getInput() {
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
  @Override
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
  @Override
  public IPromise<T, T> error(final Throwable t) {
    Promise<T> _xblockexpression = null;
    {
      nl.kii.stream.message.Error<T, T> _error = new nl.kii.stream.message.Error<T, T>(null, t);
      this.apply(_error);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
}
