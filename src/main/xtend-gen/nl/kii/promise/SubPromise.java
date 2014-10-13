package nl.kii.promise;

import nl.kii.promise.BasePromise;
import nl.kii.promise.IPromise;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class SubPromise<R extends Object, T extends Object> extends BasePromise<R, T> {
  protected final IPromise<R, ?> root;
  
  /**
   * Constructor for easily creating a child promise
   */
  public SubPromise(final IPromise<R, ?> parentPromise) {
    IPromise<R, ?> _root = parentPromise.getRoot();
    this.root = _root;
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        SubPromise.this.error(it);
      }
    };
    parentPromise.onError(_function);
  }
  
  public IPromise<R, ?> getRoot() {
    return this.root;
  }
  
  /**
   * set the promised value
   */
  public void set(final R value) {
    this.root.set(value);
  }
  
  /**
   * report an error to the listener of the promise.
   */
  public IPromise<R, T> error(final Throwable t) {
    SubPromise<R, T> _xblockexpression = null;
    {
      this.root.error(t);
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * set the promised value
   */
  void set(final R from, final T value) {
    Value<R, T> _value = new Value<R, T>(from, value);
    this.apply(_value);
  }
  
  void error(final R from, final Throwable t) {
    nl.kii.stream.Error<R, T> _error = new nl.kii.stream.Error<R, T>(from, t);
    this.apply(_error);
  }
}
