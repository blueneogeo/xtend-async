package nl.kii.stream.internal;

import com.google.common.base.Objects;
import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.act.Actor;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.IStream;
import nl.kii.stream.internal.StreamException;
import nl.kii.stream.internal.UncaughtStreamException;
import nl.kii.stream.message.Close;
import nl.kii.stream.message.Closed;
import nl.kii.stream.message.Entries;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Finish;
import nl.kii.stream.message.Next;
import nl.kii.stream.message.Overflow;
import nl.kii.stream.message.Skip;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.message.StreamMessage;
import nl.kii.stream.message.Value;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class BaseStream<I extends Object, O extends Object> extends Actor<StreamMessage> implements IStream<I, O> {
  public final static int DEFAULT_MAX_BUFFERSIZE = 1000;
  
  protected final Queue<Entry<I, O>> queue;
  
  @Atomic
  private final AtomicInteger _buffersize = new AtomicInteger(0);
  
  @Atomic
  private final AtomicBoolean _open = new AtomicBoolean(true);
  
  @Atomic
  private final AtomicBoolean _ready = new AtomicBoolean(false);
  
  @Atomic
  private final AtomicBoolean _skipping = new AtomicBoolean(false);
  
  @Atomic
  private final AtomicReference<Procedure1<? super Entry<I, O>>> _entryListener = new AtomicReference<Procedure1<? super Entry<I, O>>>();
  
  @Atomic
  private final AtomicReference<Procedure1<? super StreamEvent>> _notificationListener = new AtomicReference<Procedure1<? super StreamEvent>>();
  
  @Atomic
  private final AtomicInteger _concurrency = new AtomicInteger(0);
  
  @Atomic
  private final AtomicInteger _maxBufferSize = new AtomicInteger();
  
  @Atomic
  private final AtomicReference<String> _operation = new AtomicReference<String>();
  
  /**
   * Create the stream with a memory concurrent queue
   */
  public BaseStream() {
    this(Queues.<Entry<I, O>>newConcurrentLinkedQueue(), BaseStream.DEFAULT_MAX_BUFFERSIZE);
  }
  
  public BaseStream(final int maxBufferSize) {
    this(Queues.<Entry<I, O>>newConcurrentLinkedQueue(), maxBufferSize);
  }
  
  /**
   * Create the stream with your own provided queue.
   * Note: the queue must be threadsafe for streams to be threadsafe!
   */
  public BaseStream(final Queue<Entry<I, O>> queue, final int maxBufferSize) {
    this.queue = queue;
    this.setMaxBufferSize(Integer.valueOf(maxBufferSize));
    this.setOperation("source");
  }
  
  /**
   * Get the queue of the stream. will only be an unmodifiable view of the queue.
   */
  @Override
  public Collection<Entry<I, O>> getQueue() {
    return Collections.<Entry<I, O>>unmodifiableCollection(this.queue);
  }
  
  @Override
  public boolean isOpen() {
    return (this.getOpen()).booleanValue();
  }
  
  @Override
  public boolean isReady() {
    return (this.getReady()).booleanValue();
  }
  
  @Override
  public boolean isSkipping() {
    return (this.getSkipping()).booleanValue();
  }
  
  @Override
  public int getBufferSize() {
    return (this.getBuffersize()).intValue();
  }
  
  /**
   * Ask for the next value in the buffer to be delivered to the change listener
   */
  @Override
  public void next() {
    Next _next = new Next();
    this.apply(_next);
  }
  
  /**
   * Tell the stream to stop sending values until the next Finish(0)
   */
  @Override
  public void skip() {
    Skip _skip = new Skip();
    this.apply(_skip);
  }
  
  /**
   * Close the stream, which will stop the listener from recieving values
   */
  @Override
  public void close() {
    Close _close = new Close();
    this.apply(_close);
  }
  
  /**
   * Listen for changes on the stream. There can only be a single change listener.
   * <p>
   * this is used mostly internally, and you are encouraged to use .observe() or
   * the StreamExtensions instead.
   * @return unsubscribe function
   */
  @Override
  public Procedure0 onChange(final Procedure1<? super Entry<I, O>> entryListener) {
    this.setEntryListener(entryListener);
    final Procedure0 _function = new Procedure0() {
      @Override
      public void apply() {
        BaseStream.this.setEntryListener(null);
      }
    };
    return _function;
  }
  
  /**
   * Listen for notifications from the stream.
   * <p>
   * this is used mostly internally, and you are encouraged to use .monitor() or
   * the StreamExtensions instead.
   * @return unsubscribe function
   */
  @Override
  public Procedure0 onNotify(final Procedure1<? super StreamEvent> notificationListener) {
    this.setNotificationListener(notificationListener);
    final Procedure0 _function = new Procedure0() {
      @Override
      public void apply() {
        BaseStream.this.setNotificationListener(null);
      }
    };
    return _function;
  }
  
  /**
   * Process a single incoming stream message from the actor queue.
   */
  @Override
  protected void act(final StreamMessage entry, final Procedure0 done) {
    boolean _isOpen = this.isOpen();
    if (_isOpen) {
      boolean _matched = false;
      if (!_matched) {
        if (entry instanceof Value) {
          _matched=true;
        }
        if (!_matched) {
          if (entry instanceof Finish) {
            _matched=true;
          }
        }
        if (!_matched) {
          if (entry instanceof nl.kii.stream.message.Error) {
            _matched=true;
          }
        }
        if (_matched) {
          Integer _buffersize = this.getBuffersize();
          Integer _maxBufferSize = this.getMaxBufferSize();
          boolean _greaterEqualsThan = (_buffersize.compareTo(_maxBufferSize) >= 0);
          if (_greaterEqualsThan) {
            Overflow _overflow = new Overflow(((Entry<?, ?>)entry));
            this.notify(_overflow);
            done.apply();
            return;
          }
          this.queue.add(((Entry<I, O>)entry));
          this.incBuffersize();
          this.publishNext();
        }
      }
      if (!_matched) {
        if (entry instanceof Entries) {
          _matched=true;
          this.queue.addAll(((Entries<I, O>)entry).entries);
          int _size = ((Entries<I, O>)entry).entries.size();
          this.incBuffersize(Integer.valueOf(_size));
          this.publishNext();
        }
      }
      if (!_matched) {
        if (entry instanceof Next) {
          _matched=true;
          this.setReady(Boolean.valueOf(true));
          final boolean published = this.publishNext();
          if ((!published)) {
            this.notify(((StreamEvent)entry));
          }
        }
      }
      if (!_matched) {
        if (entry instanceof Skip) {
          _matched=true;
          boolean _isSkipping = this.isSkipping();
          if (_isSkipping) {
            return;
          } else {
            this.setSkipping(Boolean.valueOf(true));
          }
          while ((this.isSkipping() && (!this.queue.isEmpty()))) {
            Entry<I, O> _peek = this.queue.peek();
            final Entry<I, O> it = _peek;
            boolean _matched_1 = false;
            if (!_matched_1) {
              if (it instanceof Finish) {
                if ((((Finish<I, O>)it).level == 0)) {
                  _matched_1=true;
                  this.setSkipping(Boolean.valueOf(false));
                }
              }
            }
            if (!_matched_1) {
              {
                this.queue.poll();
                this.decBuffersize();
              }
            }
          }
          boolean _isSkipping_1 = this.isSkipping();
          if (_isSkipping_1) {
            this.notify(((StreamEvent)entry));
          }
        }
      }
      if (!_matched) {
        if (entry instanceof Close) {
          _matched=true;
          Closed<I, O> _closed = new Closed<I, O>();
          this.queue.add(_closed);
          this.incBuffersize();
          this.publishNext();
          this.notify(((StreamEvent)entry));
          this.setOpen(Boolean.valueOf(false));
        }
      }
    } else {
      this.queue.clear();
      this.setBuffersize(Integer.valueOf(0));
    }
    done.apply();
  }
  
  /**
   * Publish a single entry from the stream queue.
   */
  protected boolean publishNext() {
    try {
      boolean _xblockexpression = false;
      {
        boolean _or = false;
        boolean _or_1 = false;
        boolean _or_2 = false;
        boolean _isOpen = this.isOpen();
        boolean _not = (!_isOpen);
        if (_not) {
          _or_2 = true;
        } else {
          boolean _isReady = this.isReady();
          boolean _not_1 = (!_isReady);
          _or_2 = _not_1;
        }
        if (_or_2) {
          _or_1 = true;
        } else {
          Procedure1<? super Entry<I, O>> _entryListener = this.getEntryListener();
          boolean _equals = Objects.equal(_entryListener, null);
          _or_1 = _equals;
        }
        if (_or_1) {
          _or = true;
        } else {
          boolean _isEmpty = this.queue.isEmpty();
          _or = _isEmpty;
        }
        if (_or) {
          return false;
        }
        this.setReady(Boolean.valueOf(false));
        final Entry<I, O> entry = this.queue.poll();
        this.decBuffersize();
        boolean _xtrycatchfinallyexpression = false;
        try {
          boolean _xblockexpression_1 = false;
          {
            final Entry<I, O> it = entry;
            boolean _matched = false;
            if (!_matched) {
              if (it instanceof Finish) {
                _matched=true;
                if ((((Finish<I, O>)it).level == 0)) {
                  this.setSkipping(Boolean.valueOf(false));
                }
              }
            }
            Procedure1<? super Entry<I, O>> _entryListener_1 = this.getEntryListener();
            _entryListener_1.apply(entry);
            _xblockexpression_1 = true;
          }
          _xtrycatchfinallyexpression = _xblockexpression_1;
        } catch (final Throwable _t) {
          if (_t instanceof UncaughtStreamException) {
            final UncaughtStreamException e = (UncaughtStreamException)_t;
            throw e;
          } else if (_t instanceof Throwable) {
            final Throwable t = (Throwable)_t;
            boolean _xblockexpression_2 = false;
            {
              if ((entry instanceof nl.kii.stream.message.Error<?, ?>)) {
                boolean _matched = false;
                if (!_matched) {
                  if (t instanceof StreamException) {
                    _matched=true;
                    throw t;
                  }
                }
                String _operation = this.getOperation();
                throw new StreamException(_operation, entry, t);
              }
              final boolean nextCalled = this.isReady();
              this.setReady(Boolean.valueOf(true));
              boolean _matched_1 = false;
              if (!_matched_1) {
                if (entry instanceof Value) {
                  _matched_1=true;
                  String _operation_1 = this.getOperation();
                  StreamException _streamException = new StreamException(_operation_1, entry, t);
                  nl.kii.stream.message.Error<I, Object> _error = new nl.kii.stream.message.Error<I, Object>(((Value<I, O>)entry).from, _streamException);
                  this.apply(_error);
                }
              }
              if (!_matched_1) {
                if (entry instanceof nl.kii.stream.message.Error) {
                  _matched_1=true;
                  String _operation_1 = this.getOperation();
                  StreamException _streamException = new StreamException(_operation_1, entry, t);
                  nl.kii.stream.message.Error<I, Object> _error = new nl.kii.stream.message.Error<I, Object>(((nl.kii.stream.message.Error<I, O>)entry).from, _streamException);
                  this.apply(_error);
                }
              }
              if ((!nextCalled)) {
                this.next();
              }
              _xblockexpression_2 = false;
            }
            _xtrycatchfinallyexpression = _xblockexpression_2;
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
        _xblockexpression = _xtrycatchfinallyexpression;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * helper function for informing the notify listener
   */
  protected void notify(final StreamEvent command) {
    Procedure1<? super StreamEvent> _notificationListener = this.getNotificationListener();
    boolean _notEquals = (!Objects.equal(_notificationListener, null));
    if (_notEquals) {
      Procedure1<? super StreamEvent> _notificationListener_1 = this.getNotificationListener();
      _notificationListener_1.apply(command);
    }
  }
  
  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Stream { operation: ");
    String _operation = this.getOperation();
    _builder.append(_operation, "");
    _builder.append(", open: ");
    boolean _isOpen = this.isOpen();
    _builder.append(_isOpen, "");
    _builder.append(", ready: ");
    boolean _isReady = this.isReady();
    _builder.append(_isReady, "");
    _builder.append(", skipping: ");
    boolean _isSkipping = this.isSkipping();
    _builder.append(_isSkipping, "");
    _builder.append(", queue: ");
    int _size = this.queue.size();
    _builder.append(_size, "");
    _builder.append(", hasListener: ");
    Procedure1<? super Entry<I, O>> _entryListener = this.getEntryListener();
    boolean _notEquals = (!Objects.equal(_entryListener, null));
    _builder.append(_notEquals, "");
    _builder.append(" }");
    return _builder.toString();
  }
  
  private void setBuffersize(final Integer value) {
    this._buffersize.set(value);
  }
  
  private Integer getBuffersize() {
    return this._buffersize.get();
  }
  
  private Integer getAndSetBuffersize(final Integer value) {
    return this._buffersize.getAndSet(value);
  }
  
  private Integer incBuffersize() {
    return this._buffersize.incrementAndGet();
  }
  
  private Integer decBuffersize() {
    return this._buffersize.decrementAndGet();
  }
  
  private Integer incBuffersize(final Integer value) {
    return this._buffersize.addAndGet(value);
  }
  
  private void setOpen(final Boolean value) {
    this._open.set(value);
  }
  
  private Boolean getOpen() {
    return this._open.get();
  }
  
  private Boolean getAndSetOpen(final Boolean value) {
    return this._open.getAndSet(value);
  }
  
  private void setReady(final Boolean value) {
    this._ready.set(value);
  }
  
  private Boolean getReady() {
    return this._ready.get();
  }
  
  private Boolean getAndSetReady(final Boolean value) {
    return this._ready.getAndSet(value);
  }
  
  private void setSkipping(final Boolean value) {
    this._skipping.set(value);
  }
  
  private Boolean getSkipping() {
    return this._skipping.get();
  }
  
  private Boolean getAndSetSkipping(final Boolean value) {
    return this._skipping.getAndSet(value);
  }
  
  private void setEntryListener(final Procedure1<? super Entry<I, O>> value) {
    this._entryListener.set(value);
  }
  
  private Procedure1<? super Entry<I, O>> getEntryListener() {
    return this._entryListener.get();
  }
  
  private Procedure1<? super Entry<I, O>> getAndSetEntryListener(final Procedure1<? super Entry<I, O>> value) {
    return this._entryListener.getAndSet(value);
  }
  
  private void setNotificationListener(final Procedure1<? super StreamEvent> value) {
    this._notificationListener.set(value);
  }
  
  private Procedure1<? super StreamEvent> getNotificationListener() {
    return this._notificationListener.get();
  }
  
  private Procedure1<? super StreamEvent> getAndSetNotificationListener(final Procedure1<? super StreamEvent> value) {
    return this._notificationListener.getAndSet(value);
  }
  
  public void setConcurrency(final Integer value) {
    this._concurrency.set(value);
  }
  
  public Integer getConcurrency() {
    return this._concurrency.get();
  }
  
  protected Integer getAndSetConcurrency(final Integer value) {
    return this._concurrency.getAndSet(value);
  }
  
  protected Integer incConcurrency() {
    return this._concurrency.incrementAndGet();
  }
  
  protected Integer decConcurrency() {
    return this._concurrency.decrementAndGet();
  }
  
  protected Integer incConcurrency(final Integer value) {
    return this._concurrency.addAndGet(value);
  }
  
  public void setMaxBufferSize(final Integer value) {
    this._maxBufferSize.set(value);
  }
  
  public Integer getMaxBufferSize() {
    return this._maxBufferSize.get();
  }
  
  protected Integer getAndSetMaxBufferSize(final Integer value) {
    return this._maxBufferSize.getAndSet(value);
  }
  
  protected Integer incMaxBufferSize() {
    return this._maxBufferSize.incrementAndGet();
  }
  
  protected Integer decMaxBufferSize() {
    return this._maxBufferSize.decrementAndGet();
  }
  
  protected Integer incMaxBufferSize(final Integer value) {
    return this._maxBufferSize.addAndGet(value);
  }
  
  public void setOperation(final String value) {
    this._operation.set(value);
  }
  
  public String getOperation() {
    return this._operation.get();
  }
  
  protected String getAndSetOperation(final String value) {
    return this._operation.getAndSet(value);
  }
}
