package nl.kii.promise;

public class Promise<T extends java.lang.Object> /* implements FixedBasePromise<T, T>  */{
  public Promise<T> getInput() {
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
  public Object set(final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\n== cannot be resolved."
      + "\nNullPointerException cannot be resolved."
      + "\nThe method apply is undefined for the type Promise");
  }
  
  /**
   * report an error to the listener of the promise.
   */
  public Promise<T> error(final /* Throwable */Object t) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method apply is undefined for the type Promise");
  }
}
