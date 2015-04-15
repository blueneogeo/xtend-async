package nl.kii.promise.internal;

import nl.kii.promise.IPromise;
import nl.kii.promise.internal.SubPromise;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Value;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * A Task is a promise that some task gets done.
 * It has no result, it can just be completed or have an error.
 * A SubTask is a task based on a promise/task.
 */
@SuppressWarnings("all")
public class SubTask<I extends Object> extends SubPromise<I, Boolean> {
  public SubTask() {
    super();
  }
  
  public SubTask(final IPromise<I, ?> parentPromise) {
    super(parentPromise);
  }
  
  public void complete(final I from) {
    Value<I, Boolean> _value = new Value<I, Boolean>(from, Boolean.valueOf(true));
    this.apply(_value);
  }
  
  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Task { fulfilled: ");
    Boolean _fulfilled = this.getFulfilled();
    _builder.append(_fulfilled, "");
    _builder.append(" ");
    {
      Entry<I, Boolean> _get = this.get();
      if ((_get instanceof nl.kii.stream.message.Error<?, ?>)) {
        _builder.append(", error: ");
        Entry<I, Boolean> _get_1 = this.get();
        _builder.append(((nl.kii.stream.message.Error<?, ?>) _get_1).error, "");
      }
    }
    _builder.append(" }");
    return _builder.toString();
  }
}
