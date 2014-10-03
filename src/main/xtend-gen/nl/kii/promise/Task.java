package nl.kii.promise;

import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A Task is a promise that some task gets done. It has no result, it can just be completed or have an error.
 */
@SuppressWarnings("all")
public class Task extends Promise<Boolean> {
  public Task() {
  }
  
  public Task(final IPromise<?> parentPromise) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        Task.this.error(it);
      }
    };
    parentPromise.onError(_function);
  }
  
  public Task complete() {
    Task _xblockexpression = null;
    {
      this.set(Boolean.valueOf(true));
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  public String toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nIncorrect number of arguments for type Error<R, T>; it cannot be parameterized with arguments <? >"
      + "\nIncorrect number of arguments for type Error<R, T>; it cannot be parameterized with arguments <? >");
  }
}
