package nl.kii.promise;

import nl.kii.promise.Promise;

/**
 * A Task is a promise that some task gets done. It has no result, it can just be completed or have an error.
 */
public class Task /* extends /* Promise<Boolean> */  */{
  public Task complete() {
    Task _xblockexpression = null;
    {
      this.set(boolean.valueOf(true));
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public java.lang.CharSequence toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field fulfilled is undefined for the type Task"
      + "\nThe method or field get is undefined for the type Task"
      + "\nThe method or field get is undefined for the type Task");
  }
}
