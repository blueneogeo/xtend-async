package nl.kii.stream;

import nl.kii.stream.BaseStream;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Value;

@SuppressWarnings("all")
public class SubStream<I extends Object, O extends Object> extends BaseStream<I, O> {
  protected final IStream<I, I> input;
  
  public SubStream(final IStream<I, ?> parent) {
    IStream<I, I> _input = parent.getInput();
    this.input = _input;
    Integer _concurrency = parent.getConcurrency();
    this.setConcurrency(_concurrency);
  }
  
  public SubStream(final IStream<I, ?> parent, final int maxSize) {
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
  public void error(final Throwable t) {
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
  public void error(final I from, final Throwable error) {
    nl.kii.stream.Error<I, Object> _error = new nl.kii.stream.Error<I, Object>(from, error);
    this.apply(_error);
  }
  
  /**
   * Tell the stream the current batch of data is finished. The same as finish(0).
   */
  public void finish(final I from) {
    Finish<I, Object> _finish = new Finish<I, Object>(from, 0);
    this.apply(_finish);
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final I from, final int level) {
    Finish<I, Object> _finish = new Finish<I, Object>(from, level);
    this.apply(_finish);
  }
}
