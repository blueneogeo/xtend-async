package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.StreamException;
import nl.kii.stream.Value;
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
public class StreamOLDEST<T extends Object> implements Procedure1<Entry<T>> {
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
  public StreamOLDEST() {
    this(null);
  }
  
  /**
   * Creates a new Stream that is connected to a parentStream.
   */
  public StreamOLDEST(final StreamOLDEST<?> parentStream) {
    this(parentStream, new Function0<ConcurrentLinkedQueue<Entry<T>>>() {
      public ConcurrentLinkedQueue<Entry<T>> apply() {
        return new ConcurrentLinkedQueue<Entry<T>>();
      }
    });
  }
  
  /**
   * Most detailed constructor, where you can specify your own queue factory.
   */
  public StreamOLDEST(final StreamOLDEST<?> parentStream, final Function0<? extends Queue<Entry<T>>> queueFn) {
    this.queueFn = queueFn;
    boolean _notEquals = (!Objects.equal(parentStream, null));
    if (_notEquals) {
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          parentStream.next();
        }
      };
      this.onReadyForNext(_function);
      final Procedure0 _function_1 = new Procedure0() {
        public void apply() {
          parentStream.skip();
        }
      };
      this.onSkip(_function_1);
      final Procedure0 _function_2 = new Procedure0() {
        public void apply() {
          parentStream.close();
        }
      };
      this.onClose(_function_2);
      final Procedure0 _function_3 = new Procedure0() {
        public void apply() {
          StreamOLDEST.this.finish();
          parentStream.next();
        }
      };
      parentStream.onNextFinish(_function_3);
      final Procedure1<Throwable> _function_4 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          StreamOLDEST.this.error(it);
          parentStream.next();
        }
      };
      parentStream.onNextError(_function_4);
    }
    this._open.set(true);
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
  public synchronized StreamOLDEST<T> push(final T value) {
    StreamOLDEST<T> _xblockexpression = null;
    {
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        throw new NullPointerException("cannot stream a null value");
      }
      Value<T> _value = new Value<T>(value);
      this.apply(_value);
      this.publish();
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Finish a batch of data that was pushed into the stream. Note that a finish may be called
   * more than once, indicating that multiple batches were passed. Ends any skipping.
   */
  public synchronized StreamOLDEST<T> finish() {
    StreamOLDEST<T> _xblockexpression = null;
    {
      Finish<T> _finish = new Finish<T>();
      this.apply(_finish);
      this.publish();
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Report an error to the stream. It is also pushed down substreams as a message,
   * so you can listen for errors at any point below where the error is generated
   * in a stream chain.
   */
  public synchronized Object error(final Throwable t) {
    Object _xblockexpression = null;
    {
      nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(t);
      this.apply(_error);
      this.publish();
      _xblockexpression = null;
    }
    return _xblockexpression;
  }
  
  /**
   * Queue a stream entry
   */
  public synchronized void apply(final Entry<T> entry) {
    try {
      boolean _equals = Objects.equal(entry, null);
      if (_equals) {
        throw new NullPointerException("cannot stream a null entry");
      }
      boolean _get = this._open.get();
      boolean _not = (!_get);
      if (_not) {
        throw new StreamException("cannot apply an entry to a closed stream");
      }
      boolean _equals_1 = Objects.equal(this.queue, null);
      if (_equals_1) {
        Queue<Entry<T>> _apply = this.queueFn.apply();
        this.queue = _apply;
      }
      boolean _matched = false;
      if (!_matched) {
        if (entry instanceof Finish) {
          _matched=true;
          boolean _isSkipping = this.isSkipping();
          if (_isSkipping) {
            this.setSkipping(false);
          }
        }
      }
      this.queue.add(entry);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * Discards incoming values until the stream receives a Finish. Then unskip and
   * resume normal operation.
   */
  synchronized void skip() {
    boolean _isSkipping = this.isSkipping();
    if (_isSkipping) {
      return;
    } else {
      this.setSkipping(true);
    }
    boolean _and = false;
    boolean _isSkipping_1 = this.isSkipping();
    if (!_isSkipping_1) {
      _and = false;
    } else {
      boolean _switchResult = false;
      Entry<T> _peek = this.queue.peek();
      boolean _matched = false;
      if (!_matched) {
        if (_peek instanceof Value) {
          _matched=true;
          _switchResult = true;
        }
      }
      if (!_matched) {
        if (_peek instanceof nl.kii.stream.Error) {
          _matched=true;
          _switchResult = true;
        }
      }
      if (!_matched) {
        if (_peek instanceof Finish) {
          _matched=true;
          _switchResult = false;
        }
      }
      if (!_matched) {
        if (Objects.equal(_peek, null)) {
          _matched=true;
          _switchResult = false;
        }
      }
      _and = _switchResult;
    }
    boolean _while = _and;
    while (_while) {
      this.queue.poll();
      boolean _and_1 = false;
      boolean _isSkipping_2 = this.isSkipping();
      if (!_isSkipping_2) {
        _and_1 = false;
      } else {
        boolean _switchResult_1 = false;
        Entry<T> _peek_1 = this.queue.peek();
        boolean _matched_1 = false;
        if (!_matched_1) {
          if (_peek_1 instanceof Value) {
            _matched_1=true;
            _switchResult_1 = true;
          }
        }
        if (!_matched_1) {
          if (_peek_1 instanceof nl.kii.stream.Error) {
            _matched_1=true;
            _switchResult_1 = true;
          }
        }
        if (!_matched_1) {
          if (_peek_1 instanceof Finish) {
            _matched_1=true;
            _switchResult_1 = false;
          }
        }
        if (!_matched_1) {
          if (Objects.equal(_peek_1, null)) {
            _matched_1=true;
            _switchResult_1 = false;
          }
        }
        _and_1 = _switchResult_1;
      }
      _while = _and_1;
    }
    Entry<T> _peek_1 = this.queue.peek();
    if ((_peek_1 instanceof Finish<?>)) {
      this.setSkipping(false);
    }
    boolean _and_1 = false;
    boolean _isSkipping_2 = this.isSkipping();
    if (!_isSkipping_2) {
      _and_1 = false;
    } else {
      boolean _notEquals = (!Objects.equal(this.onSkip, null));
      _and_1 = _notEquals;
    }
    if (_and_1) {
      this.onSkip.apply();
    }
  }
  
  /**
   * Indicate that the listener is ready for the next value
   */
  synchronized void next() {
    this.setReadyForNext(true);
    this.publish();
  }
  
  /**
   * Closes a stream and stops it being possible to push new entries to the stream or have
   * the stream have any output. In most cases it is unnecessary to close a stream. However
   * some
   */
  public synchronized void close() {
    this._open.set(false);
    boolean _notEquals = (!Objects.equal(this.onClose, null));
    if (_notEquals) {
      this.onClose.apply();
    }
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
  StreamOLDEST<T> onNextFinish(final Procedure0 listener) {
    StreamOLDEST<T> _xblockexpression = null;
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
  StreamOLDEST<T> onNextError(final Procedure1<? super Throwable> listener) {
    StreamOLDEST<T> _xblockexpression = null;
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
  StreamOLDEST<T> onSkip(final Procedure0 listener) {
    StreamOLDEST<T> _xblockexpression = null;
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
  StreamOLDEST<T> onReadyForNext(final Procedure0 listener) {
    StreamOLDEST<T> _xblockexpression = null;
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
  public StreamOLDEST<T> onClose(final Procedure0 listener) {
    StreamOLDEST<T> _xblockexpression = null;
    {
      this.onClose = listener;
      _xblockexpression = this;
    }
    return _xblockexpression;
  }
  
  /**
   * Try to publish the next value from the queue or the parent stream
   */
  synchronized void publish() {
    boolean _or = false;
    boolean _isReadyForNext = this.isReadyForNext();
    boolean _not = (!_isReadyForNext);
    if (_not) {
      _or = true;
    } else {
      boolean _isOpen = this.isOpen();
      boolean _not_1 = (!_isOpen);
      _or = _not_1;
    }
    if (_or) {
      return;
    }
    boolean _and = false;
    boolean _notEquals = (!Objects.equal(this.queue, null));
    if (!_notEquals) {
      _and = false;
    } else {
      boolean _isEmpty = this.queue.isEmpty();
      boolean _not_2 = (!_isEmpty);
      _and = _not_2;
    }
    if (_and) {
      Entry<T> _poll = this.queue.poll();
      this.publish(_poll);
    } else {
      boolean _notEquals_1 = (!Objects.equal(this.onReadyForNext, null));
      if (_notEquals_1) {
        this.onReadyForNext.apply();
      }
    }
  }
  
  /**
   * Send an entry directly (no queue) to the listeners
   * (onValue, onError, onFinish). If a value was processed,
   * ready is set to false again, since the value was published.
   * @return true if a value was published
   */
  protected synchronized boolean publish(final Entry<T> entry) {
    try {
      boolean _switchResult = false;
      final Entry<T> it = entry;
      boolean _matched = false;
      if (!_matched) {
        if (it instanceof Value) {
          _matched=true;
          boolean _xblockexpression = false;
          {
            boolean _or = false;
            boolean _or_1 = false;
            boolean _equals = Objects.equal(this.onValue, null);
            if (_equals) {
              _or_1 = true;
            } else {
              boolean _isReadyForNext = this.isReadyForNext();
              boolean _not = (!_isReadyForNext);
              _or_1 = _not;
            }
            if (_or_1) {
              _or = true;
            } else {
              boolean _isSkipping = this.isSkipping();
              _or = _isSkipping;
            }
            if (_or) {
              return false;
            }
            boolean applied = false;
            try {
              this.setReadyForNext(false);
              this.onValue.apply(((Value<T>)it).value);
              applied = true;
            } catch (final Throwable _t) {
              if (_t instanceof Exception) {
                final Exception e = (Exception)_t;
                boolean _notEquals = (!Objects.equal(this.onError, null));
                if (_notEquals) {
                  this.onError.apply(e);
                } else {
                  throw e;
                }
              } else {
                throw Exceptions.sneakyThrow(_t);
              }
            }
            _xblockexpression = applied;
          }
          _switchResult = _xblockexpression;
        }
      }
      if (!_matched) {
        if (it instanceof Finish) {
          _matched=true;
          boolean _xblockexpression = false;
          {
            boolean _isSkipping = this.isSkipping();
            if (_isSkipping) {
              this.setSkipping(false);
            }
            boolean _notEquals = (!Objects.equal(this.onFinish, null));
            if (_notEquals) {
              this.onFinish.apply();
            }
            _xblockexpression = false;
          }
          _switchResult = _xblockexpression;
        }
      }
      if (!_matched) {
        if (it instanceof nl.kii.stream.Error) {
          _matched=true;
          boolean _xblockexpression = false;
          {
            boolean _notEquals = (!Objects.equal(this.onError, null));
            if (_notEquals) {
              this.onError.apply(((nl.kii.stream.Error<T>)it).error);
            }
            _xblockexpression = false;
          }
          _switchResult = _xblockexpression;
        }
      }
      return _switchResult;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    Class<? extends StreamOLDEST> _class = this.getClass();
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
