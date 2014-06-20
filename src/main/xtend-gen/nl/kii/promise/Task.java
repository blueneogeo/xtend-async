package nl.kii.promise;

import nl.kii.promise.Promise;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class Task extends Promise<Boolean> {
  public Task() {
  }
  
  public Task(final Promise<?> parentPromise) {
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
    boolean _isFulfilled = this.isFulfilled();
    _builder.append(_isFulfilled, "");
    _builder.append(" }");
    return _builder.toString();
  }
}
