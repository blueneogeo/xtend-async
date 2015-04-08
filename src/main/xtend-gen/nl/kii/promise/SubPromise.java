package nl.kii.promise;

import nl.kii.promise.BasePromise;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.stream.message.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class SubPromise<R extends Object, T extends Object> extends BasePromise<R, T> {
  protected final IPromise<R, ?> root;
  
  public SubPromise() {
    this.root = null;
  }
  
  /**
   * Create a promise that was based on a parent value
   */
  public SubPromise(final R parentValue) {
    this(new Promise<R>(parentValue));
  }
  
  /**
   * Constructor for easily creating a child promise.
   */
  public SubPromise(final IPromise<R, ?> parentPromise) {
    IPromise<R, ?> _root = parentPromise.getRoot();
    this.root = _root;
    final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
      @Override
      public void apply(final R i, final Throwable it) {
        SubPromise.this.error(i, it);
      }
    };
    this.root.onError(_function);
  }
  
  @Override
  public IPromise<R, ?> getRoot() {
    return this.root;
  }
  
  /**
   * set the promised value
   */
  @Override
  public void set(final R value) {
    if (this.root!=null) {
      this.root.set(value);
    }
  }
  
  /**
   * report an error to the listener of the promise.
   */
  @Override
  public IPromise<R, T> error(final Throwable t) {
    SubPromise<R, T> _xblockexpression = null;
    {
      if (this.root!=null) {
        this.root.error(t);
      }
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * set the promised value
   */
  public void set(final R from, final T value) {
    Value<R, T> _value = new Value<R, T>(from, value);
    this.apply(_value);
  }
  
  public void error(final R from, final Throwable t) {
    nl.kii.stream.message.Error<R, T> _error = new nl.kii.stream.message.Error<R, T>(from, t);
    this.apply(_error);
  }
}
