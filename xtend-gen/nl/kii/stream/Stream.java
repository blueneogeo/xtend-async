package nl.kii.stream;

import nl.kii.stream.IStream;
import nl.kii.stream.internal.BaseStream;
import nl.kii.stream.message.Finish;
import nl.kii.stream.message.Value;

public class Stream<T extends java.lang.Object> extends BaseStream<T, T> {
  public IStream<T, T> getInput() {
    return this;
  }
  
  /**
   * Queue a value on the stream for pushing to the listener
   */
  public void push(final T value) {
    Value<T, T> _value = new Value<T, T>(value, value);
    this.apply(_value);
  }
  
  /**
   * Tell the stream an error occurred. the error will not be thrown directly,
   * but passed and can be listened for down the stream.
   */
  public void error(final /* Throwable */Object error) {
    nl.kii.stream.message.Error<java.lang.Object, java.lang.Object> _error = new nl.kii.stream.message.Error<java.lang.Object, java.lang.Object>(null, error);
    this.apply(_error);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  public void finish() {
    Finish<java.lang.Object, java.lang.Object> _finish = new Finish<java.lang.Object, java.lang.Object>(null, 0);
    this.apply(_finish);
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final int level) {
    Finish<java.lang.Object, java.lang.Object> _finish = new Finish<java.lang.Object, java.lang.Object>(null, level);
    this.apply(_finish);
  }
}
