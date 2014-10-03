package nl.kii.stream;

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
import nl.kii.promise.Task;
import nl.kii.stream.Close;
import nl.kii.stream.Closed;
import nl.kii.stream.Entries;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Next;
import nl.kii.stream.Overflow;
import nl.kii.stream.Skip;
import nl.kii.stream.StreamException;
import nl.kii.stream.StreamMessage;
import nl.kii.stream.StreamMonitor;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.StreamObserver;
import nl.kii.stream.UncaughtStreamException;
import nl.kii.stream.Value;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class BaseStream<R extends Object, T extends Object> extends Actor<StreamMessage> implements IStream<R, T> {
  public final static int DEFAULT_MAX_BUFFERSIZE = 1000;
  
  protected final Queue<Entry<R, T>> queue;
  
  @Atomic
  private final AtomicInteger _buffersize = new AtomicInteger(0);
  
  @Atomic
  private final AtomicBoolean _open = new AtomicBoolean(true);
  
  @Atomic
  private final AtomicBoolean _ready = new AtomicBoolean(false);
  
  @Atomic
  private final AtomicBoolean _skipping = new AtomicBoolean(false);
  
  @Atomic
  private final AtomicReference<Procedure1<? super Entry<R, T>>> _entryListener = new AtomicReference<Procedure1<? super Entry<R, T>>>();
  
  @Atomic
  private final AtomicReference<Procedure1<? super StreamNotification>> _notificationListener = new AtomicReference<Procedure1<? super StreamNotification>>();
  
  @Atomic
  private final AtomicInteger _maxBufferSize = new AtomicInteger();
  
  @Atomic
  private final AtomicReference<String> _operation = new AtomicReference<String>();
  
  /**
   * create the stream with a memory concurrent queue
   */
  public BaseStream() {
    this(Queues.<Entry<R, T>>newConcurrentLinkedQueue(), BaseStream.DEFAULT_MAX_BUFFERSIZE);
  }
  
  public BaseStream(final int maxBufferSize) {
    this(Queues.<Entry<R, T>>newConcurrentLinkedQueue(), maxBufferSize);
  }
  
  /**
   * create the stream with your own provided queue. Note: the queue must be threadsafe!
   */
  public BaseStream(final Queue<Entry<R, T>> queue, final int maxBufferSize) {
    this.queue = queue;
    this.setMaxBufferSize(Integer.valueOf(maxBufferSize));
  }
  
  /**
   * get the queue of the stream. will only be an unmodifiable view of the queue.
   */
  public Collection<Entry<R, T>> getQueue() {
    return Collections.<Entry<R, T>>unmodifiableCollection(this.queue);
  }
  
  public boolean isOpen() {
    return (this.getOpen()).booleanValue();
  }
  
  public boolean isReady() {
    return (this.getReady()).booleanValue();
  }
  
  public boolean isSkipping() {
    return (this.getSkipping()).booleanValue();
  }
  
  public int getBufferSize() {
    return (this.getBuffersize()).intValue();
  }
  
  /**
   * Ask for the next value in the buffer to be delivered to the change listener
   */
  public void next() {
    Next _next = new Next();
    this.apply(_next);
  }
  
  /**
   * Tell the stream to stop sending values until the next Finish(0)
   */
  public void skip() {
    Skip _skip = new Skip();
    this.apply(_skip);
  }
  
  /**
   * Close the stream, which will stop the listener from recieving values
   */
  public void close() {
    Close _close = new Close();
    this.apply(_close);
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
  public void finish() {
    Finish<Object, Object> _finish = new Finish<Object, Object>(0);
    this.apply(_finish);
  }
  
  /**
   * Tell the stream a batch of the given level has finished.
   */
  public void finish(final int level) {
    Finish<Object, Object> _finish = new Finish<Object, Object>(level);
    this.apply(_finish);
  }
  
  /**
   * Listen for changes on the stream. There can only be a single change listener.
   * <p>
   * this is used mostly internally, and you are encouraged to use .observe() or
   * the StreamExtensions instead.
   * @return unsubscribe function
   */
  public Procedure0 onChange(final Procedure1<? super Entry<R, T>> entryListener) {
    this.setEntryListener(entryListener);
    final Procedure0 _function = new Procedure0() {
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
  public Procedure0 onNotify(final Procedure1<? super StreamNotification> notificationListener) {
    this.setNotificationListener(notificationListener);
    final Procedure0 _function = new Procedure0() {
      public void apply() {
        BaseStream.this.setNotificationListener(null);
      }
    };
    return _function;
  }
  
  /**
   * Observe the entries coming off this stream using a StreamObserver.
   * Note that you can only have ONE stream observer for every stream!
   * If you want more than one observer, you can split the stream.
   * <p>
   * If you are using Xtend, it is recommended to use the StreamExtensions.on [ ]
   * instead, for a more concise and elegant builder syntax.
   * <p>
   * @return a Task that can be listened to for an error, or for completion if
   * all values were processed (until finish or close).
   * <p>
   * @throws UncaughtStreamException if you have no onError listener(s) for the returned task.
   * <p>
   * Even if you process an error, the error is always exported. If the task has
   * an error listener, the error is passed to that task. If the task has no
   * error listener, then an UncaughtStreamException will be thrown.
   * <p>
   * To prevent errors from passing down the stream, you have to filter them. You can do this
   * by using the StreamExtensions.onError[] extension (or write your own filter).
   */
  public Task observe(final StreamObserver<R, T> observer) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      this.setOperation("observe");
      final Procedure1<Entry<R, T>> _function = new Procedure1<Entry<R, T>>() {
        public void apply(final Entry<R, T> entry) {
          try {
            final Entry<R, T> it = entry;
            boolean _matched = false;
            if (!_matched) {
              if (it instanceof Value) {
                _matched=true;
                observer.onValue(((Value<R, T>)it).from, ((Value<R, T>)it).value);
              }
            }
            if (!_matched) {
              if (it instanceof Finish) {
                _matched=true;
                observer.onFinish(((Finish<R, T>)it).level);
                if ((((Finish<R, T>)it).level == 0)) {
                  task.complete();
                }
              }
            }
            if (!_matched) {
              if (it instanceof nl.kii.stream.Error) {
                _matched=true;
                final boolean escalate = observer.onError(((nl.kii.stream.Error<R, T>)it).from, ((nl.kii.stream.Error<R, T>)it).error);
                if (escalate) {
                  Boolean _hasErrorHandler = task.getHasErrorHandler();
                  if ((_hasErrorHandler).booleanValue()) {
                    String _operation = BaseStream.this.getOperation();
                    StreamException _streamException = new StreamException(_operation, entry, ((nl.kii.stream.Error<R, T>)it).error);
                    task.error(_streamException);
                  } else {
                    String _operation_1 = BaseStream.this.getOperation();
                    throw new UncaughtStreamException(_operation_1, entry, ((nl.kii.stream.Error<R, T>)it).error);
                  }
                }
              }
            }
            if (!_matched) {
              if (it instanceof Closed) {
                _matched=true;
                observer.onClosed();
                task.complete();
              }
            }
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.onChange(_function);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Monitor commands given to this stream.
   */
  public void monitor(final StreamMonitor monitor) {
    final Procedure1<StreamNotification> _function = new Procedure1<StreamNotification>() {
      public void apply(final StreamNotification notification) {
        try {
          try {
            final StreamNotification it = notification;
            boolean _matched = false;
            if (!_matched) {
              if (it instanceof Next) {
                _matched=true;
                monitor.onNext();
              }
            }
            if (!_matched) {
              if (it instanceof Skip) {
                _matched=true;
                monitor.onSkip();
              }
            }
            if (!_matched) {
              if (it instanceof Close) {
                _matched=true;
                monitor.onClose();
              }
            }
            if (!_matched) {
              if (it instanceof Overflow) {
                _matched=true;
                monitor.onOverflow(((Overflow)it).entry);
              }
            }
          } catch (final Throwable _t) {
            if (_t instanceof UncaughtStreamException) {
              final UncaughtStreamException t = (UncaughtStreamException)_t;
              throw t;
            } else if (_t instanceof Exception) {
              final Exception t_1 = (Exception)_t;
              String _operation = BaseStream.this.getOperation();
              throw new StreamException(_operation, null, t_1);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    this.onNotify(_function);
  }
  
  /**
   * Process a single incoming stream message from the actor queue.
   */
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
          if (entry instanceof nl.kii.stream.Error) {
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
          this.queue.add(((Entry<R, T>)entry));
          this.incBuffersize();
          this.publishNext();
        }
      }
      if (!_matched) {
        if (entry instanceof Entries) {
          _matched=true;
          this.queue.addAll(((Entries<R, T>)entry).entries);
          int _size = ((Entries<R, T>)entry).entries.size();
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
            this.notify(((StreamNotification)entry));
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
            Entry<R, T> _peek = this.queue.peek();
            final Entry<R, T> it = _peek;
            boolean _matched_1 = false;
            if (!_matched_1) {
              if (it instanceof Finish) {
                if ((((Finish<R, T>)it).level == 0)) {
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
            this.notify(((StreamNotification)entry));
          }
        }
      }
      if (!_matched) {
        if (entry instanceof Close) {
          _matched=true;
          Closed<R, T> _closed = new Closed<R, T>();
          this.queue.add(_closed);
          this.incBuffersize();
          this.publishNext();
          this.notify(((StreamNotification)entry));
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
      boolean _xifexpression = false;
      boolean _and = false;
      boolean _and_1 = false;
      boolean _and_2 = false;
      boolean _isOpen = this.isOpen();
      if (!_isOpen) {
        _and_2 = false;
      } else {
        boolean _isReady = this.isReady();
        _and_2 = _isReady;
      }
      if (!_and_2) {
        _and_1 = false;
      } else {
        Procedure1<? super Entry<R, T>> _entryListener = this.getEntryListener();
        boolean _notEquals = (!Objects.equal(_entryListener, null));
        _and_1 = _notEquals;
      }
      if (!_and_1) {
        _and = false;
      } else {
        boolean _isEmpty = this.queue.isEmpty();
        boolean _not = (!_isEmpty);
        _and = _not;
      }
      if (_and) {
        boolean _xblockexpression = false;
        {
          this.setReady(Boolean.valueOf(false));
          final Entry<R, T> entry = this.queue.poll();
          this.decBuffersize();
          boolean _xtrycatchfinallyexpression = false;
          try {
            boolean _xblockexpression_1 = false;
            {
              final Entry<R, T> it = entry;
              boolean _matched = false;
              if (!_matched) {
                if (it instanceof Finish) {
                  _matched=true;
                  if ((((Finish<R, T>)it).level == 0)) {
                    this.setSkipping(Boolean.valueOf(false));
                  }
                }
              }
              Procedure1<? super Entry<R, T>> _entryListener_1 = this.getEntryListener();
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
                if ((entry instanceof nl.kii.stream.Error<?, ?>)) {
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
                this.setReady(Boolean.valueOf(true));
                boolean _matched_1 = false;
                if (!_matched_1) {
                  if (entry instanceof Value) {
                    _matched_1=true;
                    String _operation_1 = this.getOperation();
                    StreamException _streamException = new StreamException(_operation_1, entry, t);
                    nl.kii.stream.Error<R, Object> _error = new nl.kii.stream.Error<R, Object>(((Value<R, T>)entry).from, _streamException);
                    this.apply(_error);
                  }
                }
                if (!_matched_1) {
                  if (entry instanceof nl.kii.stream.Error) {
                    _matched_1=true;
                    String _operation_1 = this.getOperation();
                    StreamException _streamException = new StreamException(_operation_1, entry, t);
                    nl.kii.stream.Error<R, Object> _error = new nl.kii.stream.Error<R, Object>(((nl.kii.stream.Error<R, T>)entry).from, _streamException);
                    this.apply(_error);
                  }
                }
                if (!_matched_1) {
                  InputOutput.<String>println(((("help! cannot create an error! " + entry) + " gave ") + t));
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
        _xifexpression = _xblockexpression;
      } else {
        _xifexpression = false;
      }
      return _xifexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * helper function for informing the notify listener
   */
  protected void notify(final StreamNotification command) {
    Procedure1<? super StreamNotification> _notificationListener = this.getNotificationListener();
    boolean _notEquals = (!Objects.equal(_notificationListener, null));
    if (_notEquals) {
      Procedure1<? super StreamNotification> _notificationListener_1 = this.getNotificationListener();
      _notificationListener_1.apply(command);
    }
  }
  
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
    Procedure1<? super Entry<R, T>> _entryListener = this.getEntryListener();
    boolean _notEquals = (!Objects.equal(_entryListener, null));
    _builder.append(_notEquals, "");
    _builder.append(" }");
    return _builder.toString();
  }
  
  private Integer setBuffersize(final Integer value) {
    return this._buffersize.getAndSet(value);
  }
  
  private Integer getBuffersize() {
    return this._buffersize.get();
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
  
  private Boolean setOpen(final Boolean value) {
    return this._open.getAndSet(value);
  }
  
  private Boolean getOpen() {
    return this._open.get();
  }
  
  private Boolean setReady(final Boolean value) {
    return this._ready.getAndSet(value);
  }
  
  private Boolean getReady() {
    return this._ready.get();
  }
  
  private Boolean setSkipping(final Boolean value) {
    return this._skipping.getAndSet(value);
  }
  
  private Boolean getSkipping() {
    return this._skipping.get();
  }
  
  private Procedure1<? super Entry<R, T>> setEntryListener(final Procedure1<? super Entry<R, T>> value) {
    return this._entryListener.getAndSet(value);
  }
  
  private Procedure1<? super Entry<R, T>> getEntryListener() {
    return this._entryListener.get();
  }
  
  private Procedure1<? super StreamNotification> setNotificationListener(final Procedure1<? super StreamNotification> value) {
    return this._notificationListener.getAndSet(value);
  }
  
  private Procedure1<? super StreamNotification> getNotificationListener() {
    return this._notificationListener.get();
  }
  
  public Integer setMaxBufferSize(final Integer value) {
    return this._maxBufferSize.getAndSet(value);
  }
  
  public Integer getMaxBufferSize() {
    return this._maxBufferSize.get();
  }
  
  public Integer incMaxBufferSize() {
    return this._maxBufferSize.incrementAndGet();
  }
  
  public Integer decMaxBufferSize() {
    return this._maxBufferSize.decrementAndGet();
  }
  
  public Integer incMaxBufferSize(final Integer value) {
    return this._maxBufferSize.addAndGet(value);
  }
  
  public String setOperation(final String value) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe return type is incompatible with setOperation(String)");
  }
  
  public String getOperation() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe return type is incompatible with setOperation(String)");
  }
}
