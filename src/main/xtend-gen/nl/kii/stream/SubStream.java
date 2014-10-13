package nl.kii.stream;

import nl.kii.stream.BaseStream;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.Value;

@SuppressWarnings("all")
public class SubStream<R extends Object, T extends Object> extends BaseStream<R, T> {
  protected final Stream<R> root;
  
  public SubStream(final IStream<R, ?> parent) {
    Stream<R> _root = parent.getRoot();
    this.root = _root;
  }
  
  public SubStream(final IStream<R, ?> parent, final int maxSize) {
    super(maxSize);
    Stream<R> _root = parent.getRoot();
    this.root = _root;
  }
  
  public Stream<R> getRoot() {
    return this.root;
  }
  
  /**
   * Queue a value on the stream for pushing to the listener
   */
  public void push(final R value) {
    this.root.push(value);
  }
  
  /**
   * Tell the stream an error occurred. the error will not be thrown directly,
   * but passed and can be listened for down the stream.
   */
  public void error(final Throwable t) {
    this.root.error(t);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  public void finish() {
    this.root.finish();
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final int level) {
    this.root.finish(level);
  }
  
  /**
   * Queue a value on the stream for pushing to the listener
   */
  public void push(final R from, final T value) {
    Value<R, T> _value = new Value<R, T>(from, value);
    this.apply(_value);
  }
  
  /**
   * Tell the stream an error occurred. the error will not be thrown directly,
   * but passed and can be listened for down the stream.
   */
  public void error(final R from, final Throwable error) {
    nl.kii.stream.Error<R, Object> _error = new nl.kii.stream.Error<R, Object>(from, error);
    this.apply(_error);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  public void finish(final R from) {
    Finish<R, Object> _finish = new Finish<R, Object>(from, 0);
    this.apply(_finish);
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final R from, final int level) {
    Finish<R, Object> _finish = new Finish<R, Object>(from, level);
    this.apply(_finish);
  }
}
