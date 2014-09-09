package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.Task;
import nl.kii.stream.Closed;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamException;
import nl.kii.stream.StreamHandler;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A basic builder for asynchronous stream listening.
 * Combine with StreamExtensions.on like this:
 * <p>
 * <pre>
 * stream.on [
 *    each [ ... stream.next ]
 *    finish [ ... ]
 *    error [ ... ]
 * ]
 * stream.next
 * </pre>
 * <p>
 * Remember to call stream.next to start the stream!
 */
@SuppressWarnings("all")
public class StreamSubscription<T extends Object> implements StreamHandler<T> {
  protected final Stream<T> stream;
  
  private final Task task = new Task();
  
  @Atomic
  private final AtomicReference<Procedure1<T>> _valueFn = new AtomicReference<Procedure1<T>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Throwable>> _errorFn = new AtomicReference<Procedure1<Throwable>>();
  
  @Atomic
  private final AtomicReference<Procedure0> _finish0Fn = new AtomicReference<Procedure0>();
  
  @Atomic
  private final AtomicReference<Procedure1<Finish<T>>> _finishFn = new AtomicReference<Procedure1<Finish<T>>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _closedFn = new AtomicReference<Procedure1<Void>>();
  
  public StreamSubscription(final Stream<T> stream) {
    this.stream = stream;
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        StreamSubscription.this.apply(it);
      }
    };
    stream.onChange(_function);
  }
  
  /**
   * process each entry from the stream
   */
  public void apply(final Entry<T> entry) {
    final Entry<T> it = entry;
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Value) {
        _matched=true;
        try {
          Procedure1<T> _valueFn = this.getValueFn();
          if (_valueFn!=null) {
            _valueFn.apply(((Value<T>)it).value);
          }
        } catch (final Throwable _t) {
          if (_t instanceof Throwable) {
            final Throwable t = (Throwable)_t;
            StreamException _streamException = new StreamException("stream.on.each handler while processing", entry, t);
            this.stream.error(_streamException);
            this.stream.next();
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
    }
    if (!_matched) {
      if (it instanceof Finish) {
        _matched=true;
        try {
          Procedure1<Finish<T>> _finishFn = this.getFinishFn();
          if (_finishFn!=null) {
            _finishFn.apply(((Finish<T>)it));
          }
          if ((((Finish<T>)it).level == 0)) {
            Procedure0 _finish0Fn = this.getFinish0Fn();
            if (_finish0Fn!=null) {
              _finish0Fn.apply();
            }
          }
          this.task.complete();
        } catch (final Throwable _t) {
          if (_t instanceof Throwable) {
            final Throwable t = (Throwable)_t;
            StreamException _streamException = new StreamException("stream.on.finish handler while processing", entry, t);
            this.stream.error(_streamException);
            this.stream.next();
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
    }
    if (!_matched) {
      if (it instanceof nl.kii.stream.Error) {
        _matched=true;
        try {
          Procedure1<Throwable> _errorFn = this.getErrorFn();
          if (_errorFn!=null) {
            _errorFn.apply(((nl.kii.stream.Error<T>)it).error);
          }
        } catch (final Throwable _t) {
          if (_t instanceof Throwable) {
            final Throwable t = (Throwable)_t;
            StreamException _streamException = new StreamException("stream.on.error handler while processing", entry, t);
            this.task.error(_streamException);
            this.stream.next();
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        } finally {
          this.task.error(((nl.kii.stream.Error<T>)it).error);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Closed) {
        _matched=true;
        try {
          Procedure1<Void> _closedFn = this.getClosedFn();
          if (_closedFn!=null) {
            _closedFn.apply(null);
          }
          this.task.complete();
        } catch (final Throwable _t) {
          if (_t instanceof Throwable) {
            final Throwable t = (Throwable)_t;
            StreamException _streamException = new StreamException("stream.on.closed handler while processing", entry, t);
            this.stream.error(_streamException);
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
    }
  }
  
  /**
   * listen for each incoming value
   */
  public void each(final Procedure1<? super T> handler) {
    this.setValueFn(((Procedure1<T>)handler));
  }
  
  /**
   * listen for a finish of level 0
   */
  public Procedure0 finish(final Procedure0 handler) {
    return this.setFinish0Fn(handler);
  }
  
  /**
   * listen for any finish
   */
  public void finish(final Procedure1<? super Finish<T>> handler) {
    this.setFinishFn(((Procedure1<Finish<T>>)handler));
  }
  
  /**
   * listen for any uncaught errors
   */
  public void error(final Procedure1<? super Throwable> handler) {
    this.setErrorFn(((Procedure1<Throwable>)handler));
  }
  
  /**
   * listen for when the stream closes
   */
  public void closed(final Procedure1<? super Void> handler) {
    this.setClosedFn(((Procedure1<Void>)handler));
  }
  
  /**
   * get the stream the subscription is subscribed to
   */
  public Stream<T> getStream() {
    return this.stream;
  }
  
  /**
   * get the result of the subscription
   */
  public Task toTask() {
    return this.task;
  }
  
  private Procedure1<T> setValueFn(final Procedure1<T> value) {
    return this._valueFn.getAndSet(value);
  }
  
  private Procedure1<T> getValueFn() {
    return this._valueFn.get();
  }
  
  private Procedure1<Throwable> setErrorFn(final Procedure1<Throwable> value) {
    return this._errorFn.getAndSet(value);
  }
  
  private Procedure1<Throwable> getErrorFn() {
    return this._errorFn.get();
  }
  
  private Procedure0 setFinish0Fn(final Procedure0 value) {
    return this._finish0Fn.getAndSet(value);
  }
  
  private Procedure0 getFinish0Fn() {
    return this._finish0Fn.get();
  }
  
  private Procedure1<Finish<T>> setFinishFn(final Procedure1<Finish<T>> value) {
    return this._finishFn.getAndSet(value);
  }
  
  private Procedure1<Finish<T>> getFinishFn() {
    return this._finishFn.get();
  }
  
  private Procedure1<Void> setClosedFn(final Procedure1<Void> value) {
    return this._closedFn.getAndSet(value);
  }
  
  private Procedure1<Void> getClosedFn() {
    return this._closedFn.get();
  }
}
