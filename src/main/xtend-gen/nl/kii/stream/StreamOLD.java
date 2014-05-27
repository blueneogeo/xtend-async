package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamException;
import nl.kii.util.SynchronizeExt;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * <h1>what is a stream</h1>
 * 
 * A stream can receive values, and then transmit these values
 * to its listeners. To push a value into a stream, use .push().
 * To listen to values, call .each().
 * 
 * <h1>what a stream passes</h1>
 * 
 * A Stream is a publisher of three kinds of entries:
 * <li>a value
 * <li>a finish of a batch
 * <li>an error
 * 
 * <h1>finishing a batch</h1>
 * 
 * After pushing some values, you can finish that batch of values by
 * by calling .finish(). This marks the end of a batch of values that
 * you have inputted and is used as a signal to process the batch.
 * <p>
 * For example, the StreamExt.collect() extension uses it to know when
 * a bunch of values have to be collected and transformed into a list.
 * <p>
 * You can repeat this process multiple times on a stream. In the case
 * of collect(), this results in multiple lists being generated in
 * the resulting stream.
 * <p>
 * To just get the first value from a stream, call .then() or .first().
 * 
 * <h1>catching listener errors</h1>
 * 
 * If you have multiple asynchronous processes happening, it can be
 * practical to catch any errors thrown at the end of a stream chain,
 * and not inside the listeners. If you enable catchErrors, the stream
 * will catch any errors occurring in listeners and will instead pass
 * them to a listener that you can pass by calling onError().
 * 
 * <h1>extensions</h1>
 * 
 * The stream class only supports a basic publishing interface.
 * You can add extra functionality by importing extensions:
 * <li>StreamExt
 * <li>StreamPairExt
 * <p>
 * Or creating your own extensions.
 */
@SuppressWarnings("all")
public class StreamOLD<T extends Object> implements Procedure1<Entry<T>> {
  /**
   * The queue is lazily constructed using this function.
   */
  private final Function0<? extends Queue<Entry<T>>> queueFn;
  
  /**
   * The queue gets filled when there are entries entering the stream
   * even though there are no listeners yet. The queue will only grow
   * upto the maxQueueSize. If more entries enter than the size, the
   * queue will overflow and discard these later entries.
   */
  private Queue<Entry<T>> queue;
  
  /**
   * If true, the value listener is ready for a next value
   */
  private final AtomicBoolean _readyForNext = new AtomicBoolean(false);
  
  /**
   * If true, all values will be discarded upto the next incoming finish
   */
  private final AtomicBoolean _skipping = new AtomicBoolean(false);
  
  /**
   * If true, all values will be discarded upto the next incoming finish
   */
  private final AtomicBoolean _open = new AtomicBoolean(false);
  
  /**
   * Lets others listen when the stream is asked skip to the finish
   */
  private Procedure0 onSkip;
  
  /**
   * Called when this stream is out of data
   */
  private Procedure0 onReadyForNext;
  
  /**
   * Lets others listen when the stream is closed
   */
  private Procedure0 onClose;
  
  /**
   * Lets others listen for values in the stream
   */
  private Procedure1<? super T> onValue;
  
  /**
   * Lets others listen for errors occurring in the onValue listener
   */
  private Procedure1<? super Throwable> onError;
  
  /**
   * Lets others listen for the stream finishing a batch
   */
  private Procedure0 onFinish;
  
  /**
   * Creates a new Stream.
   */
  public StreamOLD() {
    this(null);
  }
  
  /**
   * Creates a new Stream that is connected to a parentStream.
   */
  public StreamOLD(final Stream<?> parentStream) {
    this(parentStream, new Function0<ConcurrentLinkedQueue<Entry<T>>>() {
      public ConcurrentLinkedQueue<Entry<T>> apply() {
        return new ConcurrentLinkedQueue<Entry<T>>();
      }
    });
  }
  
  /**
   * Most detailed constructor, where you can specify your own queue factory.
   */
  public StreamOLD(final Stream<?> parentStream, final Function0<? extends Queue<Entry<T>>> queueFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method next is undefined for the type StreamOLD"
      + "\nThe method skip is undefined for the type StreamOLD"
      + "\nThe method close is undefined for the type StreamOLD"
      + "\nThe method next is undefined for the type StreamOLD"
      + "\nThe method next is undefined for the type StreamOLD"
      + "\nInvalid number of arguments. The method onNextFinish(()=>void) is not applicable for the arguments (Stream<?>,()=>Object)"
      + "\nInvalid number of arguments. The method onNextError((Throwable)=>void) is not applicable for the arguments (Stream<?>,(Throwable)=>Object)"
      + "\nType mismatch: cannot convert from Stream<?> to (Throwable)=>void"
      + "\nType mismatch: cannot convert from Stream<?> to ()=>void");
  }
  
  protected void setReadyForNext(final boolean isReady) {
    this._readyForNext.set(isReady);
  }
  
  boolean isReadyForNext() {
    return this._readyForNext.get();
  }
  
  protected void setSkipping(final boolean isSkipping) {
    this._skipping.set(isSkipping);
  }
  
  boolean isSkipping() {
    return this._skipping.get();
  }
  
  Queue<Entry<T>> getQueue() {
    return this.queue;
  }
  
  boolean isOpen() {
    return this._open.get();
  }
  
  /**
   * Push a value into the stream, and publishes it to the listeners
   */
  public void push(final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Finish a batch of data that was pushed into the stream. Note that a finish may be called
   * more than once, indicating that multiple batches were passed. Ends any skipping.
   */
  public void finish() {
    throw new Error("Unresolved compilation problems:"
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Report an error to the stream. It is also pushed down substreams as a message,
   * so you can listen for errors at any point below where the error is generated
   * in a stream chain.
   */
  public void error(final Throwable t) {
    throw new Error("Unresolved compilation problems:"
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Queue a stream entry
   */
  public void apply(final Entry<T> entry) {
    final Procedure1<StreamOLD<T>> _function = new Procedure1<StreamOLD<T>>() {
      public void apply(final StreamOLD<T> it) {
        try {
          boolean _equals = Objects.equal(entry, null);
          if (_equals) {
            throw new NullPointerException("cannot stream a null entry");
          }
          boolean _get = it._open.get();
          boolean _not = (!_get);
          if (_not) {
            throw new StreamException("cannot apply an entry to a closed stream");
          }
          boolean _equals_1 = Objects.equal(it.queue, null);
          if (_equals_1) {
            Queue<Entry<T>> _apply = it.queueFn.apply();
            it.queue = _apply;
          }
          boolean _matched = false;
          if (!_matched) {
            if (entry instanceof Finish) {
              _matched=true;
              boolean _isSkipping = it.isSkipping();
              if (_isSkipping) {
                StreamOLD.this.setSkipping(false);
              }
            }
          }
          it.queue.add(entry);
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SynchronizeExt.<StreamOLD<T>>synchronize(this, _function);
  }
  
  /**
   * Discards incoming values until the stream receives a Finish. Then unskip and
   * resume normal operation.
   */
  void skip() {
    throw new Error("Unresolved compilation problems:"
      + "\nVoid functions cannot return a value."
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Indicate that the listener is ready for the next value
   */
  void next() {
    throw new Error("Unresolved compilation problems:"
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Closes a stream and stops it being possible to push new entries to the stream or have
   * the stream have any output. In most cases it is unnecessary to close a stream. However
   * some
   */
  public void close() {
    throw new Error("Unresolved compilation problems:"
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Listen for values coming down the stream.
   * Requires next to be called to get a value.
   */
  void onNextValue(final Procedure1<? super T> listener) {
    this.onValue = listener;
  }
  
  /**
   * Listen for a finish call being done on the stream. For each finish call,
   * the passed listener will be called with this stream.
   */
  StreamOLD<T> onNextFinish(final Procedure0 listener) {
    StreamOLD<T> _xblockexpression = null;
    {
      this.onFinish = listener;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Listen for errors in the stream or parent streams.
   * The stream only catches errors if catchErrors is set to true.
   * Automatically moves the stream forward.
   */
  StreamOLD<T> onNextError(final Procedure1<? super Throwable> listener) {
    StreamOLD<T> _xblockexpression = null;
    {
      this.onError = listener;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Let a parent stream listen when this stream skips to the finish.
   * Only supports a single listener.
   */
  StreamOLD<T> onSkip(final Procedure0 listener) {
    StreamOLD<T> _xblockexpression = null;
    {
      this.onSkip = listener;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Let a parent stream listen when this stream is asked for the next value.
   * Only supports a single listener.
   */
  StreamOLD<T> onReadyForNext(final Procedure0 listener) {
    StreamOLD<T> _xblockexpression = null;
    {
      this.onReadyForNext = listener;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Let a parent stream listen when this stream is asked for the next value.
   * Only supports a single listener.
   */
  public StreamOLD<T> onClose(final Procedure0 listener) {
    StreamOLD<T> _xblockexpression = null;
    {
      this.onClose = listener;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Try to publish the next value from the queue or the parent stream
   */
  void publish() {
    throw new Error("Unresolved compilation problems:"
      + "\nVoid functions cannot return a value."
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  /**
   * Send an entry directly (no queue) to the listeners
   * (onValue, onError, onFinish). If a value was processed,
   * ready is set to false again, since the value was published.
   * @return true if a value was published
   */
  protected boolean publish(final Entry<T> entry) {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert from (StreamOLD<T>)=>void to Procedure1<StreamOLD<T>>"
      + "\nType mismatch: cannot convert from void to boolean"
      + "\nVoid functions cannot return a value."
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects."
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects."
      + "\nThis expression is not allowed in this context, since it doesn\'t cause any side effects.");
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    Class<? extends StreamOLD> _class = this.getClass();
    String _name = _class.getName();
    _builder.append(_name, "");
    _builder.append(" { ");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t");
    _builder.append("queue size: ");
    {
      boolean _notEquals = (!Objects.equal(this.queue, null));
      if (_notEquals) {
        _builder.append(" ");
        int _length = ((Object[])Conversions.unwrapArray(this.queue, Object.class)).length;
        _builder.append(_length, "\t\t\t");
        _builder.append(" ");
      } else {
        _builder.append(" none ");
      }
    }
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t");
    _builder.append("open: ");
    boolean _isOpen = this.isOpen();
    _builder.append(_isOpen, "\t\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }
}
