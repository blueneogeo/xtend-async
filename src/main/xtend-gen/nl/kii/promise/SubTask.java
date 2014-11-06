package nl.kii.promise;

import nl.kii.promise.IPromise;
import nl.kii.promise.SubPromise;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Value;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * A Task is a promise that some task gets done. It has no result, it can just be completed or have an error.
 */
@SuppressWarnings("all")
public class SubTask<R extends Object> extends SubPromise<R, Boolean> {
  public SubTask() {
    super();
  }
  
  public SubTask(final IPromise<R, ?> parentPromise) {
    super(parentPromise);
  }
  
  public void complete(final R from) {
    Value<R, Boolean> _value = new Value<R, Boolean>(from, Boolean.valueOf(true));
    this.apply(_value);
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Task { fulfilled: ");
    Boolean _fulfilled = this.getFulfilled();
    _builder.append(_fulfilled, "");
    _builder.append(" ");
    {
      Entry<R, Boolean> _get = this.get();
      if ((_get instanceof nl.kii.stream.message.Error<?, ?>)) {
        _builder.append(", error: ");
        Entry<R, Boolean> _get_1 = this.get();
        _builder.append(((nl.kii.stream.message.Error<?, ?>) _get_1).error, "");
      }
    }
    _builder.append(" }");
    return _builder.toString();
  }
}
