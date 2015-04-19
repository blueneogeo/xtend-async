package nl.kii.stream;

import nl.kii.stream.IStream;
import nl.kii.stream.internal.BaseStream;
import nl.kii.stream.message.Finish;
import nl.kii.stream.message.Value;

@SuppressWarnings("all")
public class Stream<T extends Object> extends BaseStream<T, T> {
  @Override
  public IStream<T, T> getInput() {
    return this;
  }
  
  /**
   * Queue a value on the stream for pushing to the listener
   */
  @Override
  public void push(final T value) {
    Value<T, T> _value = new Value<T, T>(value, value);
    this.apply(_value);
  }
  
  /**
   * Tell the stream an error occurred. the error will not be thrown directly,
   * but passed and can be listened for down the stream.
   */
  @Override
  public void error(final Throwable error) {
    nl.kii.stream.message.Error<Object, Object> _error = new nl.kii.stream.message.Error<Object, Object>(null, error);
    this.apply(_error);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  @Override
  public void finish() {
    Finish<Object, Object> _finish = new Finish<Object, Object>(null, 0);
    this.apply(_finish);
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  @Override
  public void finish(final int level) {
    Finish<Object, Object> _finish = new Finish<Object, Object>(null, level);
    this.apply(_finish);
  }
}
