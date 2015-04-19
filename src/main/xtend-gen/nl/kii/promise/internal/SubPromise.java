package nl.kii.promise.internal;

import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.internal.FixedBasePromise;
import nl.kii.stream.message.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class SubPromise<I extends Object, O extends Object> extends FixedBasePromise<I, O> {
  protected final IPromise<I, ?> input;
  
  public SubPromise() {
    this.input = null;
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
    IPromise<I, ?> _input = parentPromise.getInput();
    this.input = _input;
    if (listenForErrors) {
      final Procedure2<I, Throwable> _function = new Procedure2<I, Throwable>() {
        @Override
        public void apply(final I i, final Throwable it) {
          SubPromise.this.error(i, it);
        }
      };
      this.input.on(Throwable.class, true, _function);
    }
  }
  
  @Override
  public IPromise<I, ?> getInput() {
    return this.input;
  }
  
  /**
   * set the promised value
   */
  @Override
  public void set(final I value) {
    if (this.input!=null) {
      this.input.set(value);
    }
  }
  
  /**
   * report an error to the listener of the promise.
   */
  @Override
  public IPromise<I, O> error(final Throwable t) {
    SubPromise<I, O> _xblockexpression = null;
    {
      if (this.input!=null) {
        this.input.error(t);
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
