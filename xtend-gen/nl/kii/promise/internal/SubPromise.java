package nl.kii.promise.internal;

import nl.kii.promise.IPromise;

public class SubPromise<I extends java.lang.Object, O extends java.lang.Object> /* implements FixedBasePromise<I, O>  */{
  protected final /* IPromise<I, ? extends  */Object input;
  
  public SubPromise() {
    this.input = null;
  }
  
  /**
   * Create a promise that was based on a parent value
   */
  public SubPromise(final I parentValue) {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from Promise<I> to I"
      + "\nRecursive constructor invocation");
  }
  
  /**
   * Constructor for easily creating a child promise. Listenes for errors in the parent.
   */
  public SubPromise(final /* IPromise<I, ? extends  */Object parentPromise) {
    this(parentPromise, true);
  }
  
  /**
   * Constructor to allow control of error listening
   */
  public SubPromise(final /* IPromise<I, ? extends  */Object parentPromise, final boolean listenForErrors) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Throwable is undefined for the type SubPromise"
      + "\nInvalid number of arguments. The method error(Throwable) is not applicable for the arguments (Object,Object)");
  }
  
  public /* IPromise<I, ? extends Object> */Object getInput() {
    return this.input;
  }
  
  /**
   * set the promised value
   */
  public void set(final I value) {
    if (this.input!=null) {
      this.input.set(value);
    }
  }
  
  /**
   * report an error to the listener of the promise.
   */
  public SubPromise<I, O> error(final /* Throwable */Object t) {
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
    throw new Error("Unresolved compilation problems:"
      + "\nThe method apply is undefined for the type SubPromise");
  }
  
  public void error(final I from, final /* Throwable */Object t) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method apply is undefined for the type SubPromise");
  }
}
