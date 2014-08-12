package nl.kii.promise;

import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import org.eclipse.xtend2.lib.StringConcatenation;
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
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Task { fulfilled: ");
    Boolean _fulfilled = this.getFulfilled();
    _builder.append(_fulfilled, "");
    _builder.append(" }");
    return _builder.toString();
  }
}
