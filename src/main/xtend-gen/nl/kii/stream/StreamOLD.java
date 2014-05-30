package nl.kii.stream;

import com.google.common.base.Objects;
import com.google.common.collect.Queues;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import nl.kii.stream.Close;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.StreamCommand;
import nl.kii.stream.StreamException;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure4;

@SuppressWarnings("all")
public class StreamOLD<T extends Object> implements Procedure1<Entry<T>> {
  private final Queue<Entry<T>> queue;
  
  private final AtomicBoolean open = new AtomicBoolean(true);
  
  private final AtomicBoolean skipping = new AtomicBoolean(false);
  
  private final AtomicBoolean listenerReady = new AtomicBoolean(false);
  
  private Procedure1<? super StreamCommand> commandListener;
  
  private Procedure4<? super Entry<T>, ? super Procedure0, ? super Procedure0, ? super Procedure0> entryListener;
  
  private final Procedure0 nextFn = new Procedure0() {
    public void apply() {
      Next _next = new Next();
      StreamOLD.this.perform(_next);
    }
  };
  
  private final Procedure0 skipFn = new Procedure0() {
    public void apply() {
      Skip _skip = new Skip();
      StreamOLD.this.perform(_skip);
    }
  };
  
  private final Procedure0 closeFn = new Procedure0() {
    public void apply() {
      Close _close = new Close();
      StreamOLD.this.perform(_close);
    }
  };
  
  private final ReentrantLock inputLock = new ReentrantLock();
  
  private final ReentrantLock outputLock = new ReentrantLock();
  
  private final ReentrantLock controlLock = new ReentrantLock();
  
  public StreamOLD() {
    LinkedBlockingQueue<Entry<T>> _newLinkedBlockingQueue = Queues.<Entry<T>>newLinkedBlockingQueue();
    this.queue = _newLinkedBlockingQueue;
  }
  
  public StreamOLD(final Queue<Entry<T>> queueFn) {
    this.queue = this.queue;
  }
  
  public void apply(final Entry<T> entry) {
    boolean _get = this.open.get();
    boolean _not = (!_get);
    if (_not) {
      return;
    }
    this.inputLock.lock();
    this.queue.add(entry);
    this.inputLock.unlock();
    this.publishNext();
  }
  
  public void apply(final Entry<T>... entries) {
    boolean _get = this.open.get();
    boolean _not = (!_get);
    if (_not) {
      return;
    }
    this.inputLock.lock();
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        StreamOLD.this.queue.add(it);
      }
    };
    IterableExtensions.<Entry<T>>forEach(((Iterable<Entry<T>>)Conversions.doWrapArray(entries)), _function);
    this.inputLock.unlock();
    this.publishNext();
  }
  
  public void perform(final StreamCommand cmd) {
    boolean _get = this.open.get();
    boolean _not = (!_get);
    if (_not) {
      return;
    }
    try {
      this.controlLock.lock();
      boolean _matched = false;
      if (!_matched) {
        if (cmd instanceof Next) {
          _matched=true;
          this.listenerReady.set(true);
          final boolean published = this.publishNext();
          if ((!published)) {
            this.notify(cmd);
          }
        }
      }
      if (!_matched) {
        if (cmd instanceof Skip) {
          _matched=true;
          boolean _get_1 = this.skipping.get();
          if (_get_1) {
            return;
          } else {
            this.skipping.set(true);
          }
          boolean _and = false;
          boolean _get_2 = this.skipping.get();
          if (!_get_2) {
            _and = false;
          } else {
            boolean _isEmpty = this.queue.isEmpty();
            boolean _not_1 = (!_isEmpty);
            _and = _not_1;
          }
          boolean _while = _and;
          while (_while) {
            Entry<T> _peek = this.queue.peek();
            boolean _matched_1 = false;
            if (!_matched_1) {
              if (_peek instanceof Finish) {
                _matched_1=true;
                this.skipping.set(false);
              }
            }
            if (!_matched_1) {
              this.queue.poll();
            }
            boolean _and_1 = false;
            boolean _get_3 = this.skipping.get();
            if (!_get_3) {
              _and_1 = false;
            } else {
              boolean _isEmpty_1 = this.queue.isEmpty();
              boolean _not_2 = (!_isEmpty_1);
              _and_1 = _not_2;
            }
            _while = _and_1;
          }
          boolean _get_3 = this.skipping.get();
          if (_get_3) {
            this.notify(cmd);
          }
        }
      }
      if (!_matched) {
        if (cmd instanceof Close) {
          _matched=true;
          this.open.set(false);
          this.notify(cmd);
        }
      }
    } finally {
      this.controlLock.unlock();
    }
  }
  
  /**
   * take an entry from the queue and pass it to the listener
   */
  protected boolean publishNext() {
    try {
      boolean _xtrycatchfinallyexpression = false;
      try {
        boolean _xblockexpression = false;
        {
          this.outputLock.tryLock();
          boolean _xifexpression = false;
          boolean _and = false;
          boolean _and_1 = false;
          boolean _get = this.listenerReady.get();
          if (!_get) {
            _and_1 = false;
          } else {
            boolean _notEquals = (!Objects.equal(this.entryListener, null));
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
            boolean _xblockexpression_1 = false;
            {
              this.listenerReady.set(false);
              final Entry<T> entry = this.queue.poll();
              if ((entry instanceof Finish<?>)) {
                this.skipping.set(false);
              }
              boolean _xtrycatchfinallyexpression_1 = false;
              try {
                boolean _xblockexpression_2 = false;
                {
                  this.entryListener.apply(entry, this.nextFn, this.skipFn, this.closeFn);
                  _xblockexpression_2 = true;
                }
                _xtrycatchfinallyexpression_1 = _xblockexpression_2;
              } catch (final Throwable _t) {
                if (_t instanceof Throwable) {
                  final Throwable t = (Throwable)_t;
                  boolean _xblockexpression_3 = false;
                  {
                    if ((entry instanceof nl.kii.stream.Error<?>)) {
                      throw t;
                    }
                    this.listenerReady.set(true);
                    nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(t);
                    this.apply(_error);
                    _xblockexpression_3 = false;
                  }
                  _xtrycatchfinallyexpression_1 = _xblockexpression_3;
                } else {
                  throw Exceptions.sneakyThrow(_t);
                }
              }
              _xblockexpression_1 = _xtrycatchfinallyexpression_1;
            }
            _xifexpression = _xblockexpression_1;
          } else {
            _xifexpression = false;
          }
          _xblockexpression = _xifexpression;
        }
        _xtrycatchfinallyexpression = _xblockexpression;
      } finally {
        this.outputLock.unlock();
      }
      return _xtrycatchfinallyexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * notify the commandlistener that we've performed a command
   */
  protected void notify(final StreamCommand cmd) {
    boolean _notEquals = (!Objects.equal(this.commandListener, null));
    if (_notEquals) {
      this.commandListener.apply(cmd);
    }
  }
  
  public synchronized void setListener(final Procedure4<? super Entry<T>, ? super Procedure0, ? super Procedure0, ? super Procedure0> entryListener) {
    try {
      boolean _notEquals = (!Objects.equal(this.entryListener, null));
      if (_notEquals) {
        throw new StreamException("a stream can only have a single entry listener, one was already assigned.");
      }
      this.entryListener = entryListener;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public synchronized void setCmdListener(final Procedure1<? super StreamCommand> commandListener) {
    try {
      boolean _notEquals = (!Objects.equal(this.commandListener, null));
      if (_notEquals) {
        throw new StreamException("a stream can only have a single command listener, one was already assigned.");
      }
      this.commandListener = commandListener;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * you're not getting the actual queue, just a list of what is in it
   */
  public synchronized List<Queue<Entry<T>>> getQueue() {
    return CollectionLiterals.<Queue<Entry<T>>>newImmutableList(this.queue);
  }
  
  public synchronized boolean isOpen() {
    return this.open.get();
  }
}
