package nl.kii.stream.internal;

import nl.kii.stream.IStream;
import nl.kii.stream.internal.BaseStream;
import nl.kii.stream.message.Finish;
import nl.kii.stream.message.Value;

/**
 * Streams can be chained with operations, such as map, effect, and onEach.
 * Each of these operations creates a new stream from the starting stream,
 * and these new streams are called sub streams.
 * <p>
 * Since they are based on a stream, they must be constructed with a parent stream.
 * <p>
 * Pushing a value to a substream actually pushes it into the root of the chain of streams.
 */
public class SubStream<I extends java.lang.Object, O extends java.lang.Object> extends BaseStream<I, O> {
  protected final IStream<I, I> input;
  
  public SubStream(final /* IStream<I, ? extends  */Object parent) {
    throw new Error("Unresolved compilation problems:"
      + "\nAssignment to final field");
  }
  
  public SubStream(final /* IStream<I, ? extends  */Object parent, final int maxSize) {
    super(maxSize);
    IStream<I, I> _input = parent.getInput();
    this.input = _input;
  }
  
  public IStream<I, I> getInput() {
    return this.input;
  }
  
  /**
   * Queue a value on the stream for pushing to the listener
   */
  public void push(final I value) {
    this.input.push(value);
  }
  
  /**
   * Tell the stream an error occurred. the error will not be thrown directly,
   * but passed and can be listened for down the stream.
   */
  public void error(final /* Throwable */Object t) {
    this.input.error(t);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  public void finish() {
    this.input.finish();
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final int level) {
    this.input.finish(level);
  }
  
  /**
   * Queue a value on the stream for pushing to the listener
   */
  public void push(final I from, final O value) {
    Value<I, O> _value = new Value<I, O>(from, value);
    this.apply(_value);
  }
  
  /**
   * Tell the stream an error occurred. the error will not be thrown directly,
   * but passed and can be listened for down the stream.
   */
  public void error(final I from, final /* Throwable */Object error) {
    nl.kii.stream.message.Error<I, java.lang.Object> _error = new nl.kii.stream.message.Error<I, java.lang.Object>(from, error);
    this.apply(_error);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  public void finish(final I from) {
    Finish<I, java.lang.Object> _finish = new Finish<I, java.lang.Object>(from, 0);
    this.apply(_finish);
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final I from, final int level) {
    Finish<I, java.lang.Object> _finish = new Finish<I, java.lang.Object>(from, level);
    this.apply(_finish);
  }
}
