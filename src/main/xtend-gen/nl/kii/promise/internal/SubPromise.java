package nl.kii.promise.internal;

import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.internal.FixedBasePromise;
import nl.kii.stream.message.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class SubPromise<I extends Object, O extends Object> extends FixedBasePromise<I, O> {
  protected final IPromise<I, ?> root;
  
  public SubPromise() {
    this.root = null;
  }
  
  /**
   * Create a promise that was based on a parent value
   */
  public SubPromise(final I parentValue) {
    this(new Promise<I>(parentValue));
  }
  
  /**
   * Constructor for easily creating a child promise. Listenes for errors in the parent.
   */
  public SubPromise(final IPromise<I, ?> parentPromise) {
    this(parentPromise, true);
  }
  
  /**
   * Constructor to allow control of error listening
   */
  public SubPromise(final IPromise<I, ?> parentPromise, final boolean listenForErrors) {
    IPromise<I, ?> _root = parentPromise.getRoot();
    this.root = _root;
    if (listenForErrors) {
      final Procedure2<I, Throwable> _function = new Procedure2<I, Throwable>() {
        @Override
        public void apply(final I i, final Throwable it) {
          SubPromise.this.error(i, it);
        }
      };
      this.root.on(Throwable.class, _function);
    }
  }
  
  @Override
  public IPromise<I, ?> getRoot() {
    return this.root;
  }
  
  /**
   * set the promised value
   */
  @Override
  public void set(final I value) {
    if (this.root!=null) {
      this.root.set(value);
    }
  }
  
  /**
   * report an error to the listener of the promise.
   */
  @Override
  public IPromise<I, O> error(final Throwable t) {
    SubPromise<I, O> _xblockexpression = null;
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
  public void set(final I from, final O value) {
    Value<I, O> _value = new Value<I, O>(from, value);
    this.apply(_value);
  }
  
  public void error(final I from, final Throwable t) {
    nl.kii.stream.message.Error<I, O> _error = new nl.kii.stream.message.Error<I, O>(from, t);
    this.apply(_error);
  }
}
