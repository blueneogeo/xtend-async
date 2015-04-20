package nl.kii.promise.internal;

import nl.kii.promise.IPromise;
import nl.kii.promise.internal.SubPromise;

/**
 * A Task is a promise that some task gets done.
 * It has no result, it can just be completed or have an error.
 * A SubTask is a task based on a promise/task.
 */
public class SubTask<I extends java.lang.Object> /* extends /* SubPromise<I, Boolean> */  */{
  public SubTask() {
    super();
  }
  
  public SubTask(final /* IPromise<I, ? extends  */Object parentPromise) {
    super(parentPromise);
  }
  
  public Object complete(final I from) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method apply is undefined for the type SubTask");
  }
  
  public java.lang.CharSequence toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field fulfilled is undefined for the type SubTask"
      + "\nThe method or field get is undefined for the type SubTask"
      + "\nThe method or field get is undefined for the type SubTask");
  }
}
