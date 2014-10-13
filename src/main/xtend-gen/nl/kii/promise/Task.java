package nl.kii.promise;

import nl.kii.promise.Promise;
import nl.kii.stream.Entry;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * A Task is a promise that some task gets done. It has no result, it can just be completed or have an error.
 */
@SuppressWarnings("all")
public class Task extends Promise<Boolean> {
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
    _builder.append(" ");
    {
      Entry<Boolean, Boolean> _get = this.get();
      if ((_get instanceof nl.kii.stream.Error<?, ?>)) {
        _builder.append(", error: ");
        Entry<Boolean, Boolean> _get_1 = this.get();
        _builder.append(((nl.kii.stream.Error<?, ?>) _get_1).error, "");
      }
    }
    _builder.append(" }");
    return _builder.toString();
  }
}
