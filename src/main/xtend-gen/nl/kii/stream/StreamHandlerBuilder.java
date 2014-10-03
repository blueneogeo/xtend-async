package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.IStream;
import nl.kii.stream.StreamHandler;
import nl.kii.stream.StreamObserver;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

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
public class StreamHandlerBuilder<R extends Object, T extends Object> implements StreamHandler<R, T>, StreamObserver<R, T> {
  public final IStream<R, T> stream;
  
  @Atomic
  private final AtomicReference<Procedure1<T>> _valueFn = new AtomicReference<Procedure1<T>>();
  
  @Atomic
  private final AtomicReference<Procedure2<R, T>> _valueFn2 = new AtomicReference<Procedure2<R, T>>();
  
  @Atomic
  private final AtomicReference<Function1<Throwable, Boolean>> _errorFn = new AtomicReference<Function1<Throwable, Boolean>>();
  
  @Atomic
  private final AtomicReference<Function2<R, Throwable, Boolean>> _errorFn2 = new AtomicReference<Function2<R, Throwable, Boolean>>();
  
  @Atomic
  private final AtomicReference<Procedure0> _finish0Fn = new AtomicReference<Procedure0>();
  
  @Atomic
  private final AtomicReference<Procedure1<Integer>> _finishFn = new AtomicReference<Procedure1<Integer>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _closedFn = new AtomicReference<Procedure1<Void>>();
  
  public StreamHandlerBuilder(final IStream<R, T> stream) {
    this.stream = stream;
  }
  
  /**
   * listen for each incoming value
   */
  public void each(final Procedure1<? super T> handler) {
    this.setValueFn(((Procedure1<T>)handler));
  }
  
  /**
   * listen for each incoming value
   */
  public void each(final Procedure2<? super R, ? super T> handler) {
    this.setValueFn2(((Procedure2<R, T>)handler));
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
  public void finish(final Procedure1<? super Integer> handler) {
    this.setFinishFn(((Procedure1<Integer>)handler));
  }
  
  /**
   * listen for any uncaught errors
   */
  public void error(final Function1<? super Throwable, ? extends Boolean> handler) {
    this.setErrorFn(((Function1<Throwable, Boolean>)handler));
  }
  
  /**
   * listen for any uncaught errors
   */
  public void error(final Function2<? super R, ? super Throwable, ? extends Boolean> handler) {
    this.setErrorFn2(((Function2<R, Throwable, Boolean>)handler));
  }
  
  /**
   * listen for when the stream closes
   */
  public void closed(final Procedure1<? super Void> handler) {
    this.setClosedFn(((Procedure1<Void>)handler));
  }
  
  public void onValue(final R from, final T value) {
    Procedure1<T> _valueFn = this.getValueFn();
    if (_valueFn!=null) {
      _valueFn.apply(value);
    }
    Procedure2<R, T> _valueFn2 = this.getValueFn2();
    if (_valueFn2!=null) {
      _valueFn2.apply(from, value);
    }
  }
  
  public boolean onError(final R from, final Throwable t) {
    Boolean _xblockexpression = null;
    {
      Function1<Throwable, Boolean> _errorFn = this.getErrorFn();
      if (_errorFn!=null) {
        _errorFn.apply(t);
      }
      Function2<R, Throwable, Boolean> _errorFn2 = this.getErrorFn2();
      Boolean _apply = null;
      if (_errorFn2!=null) {
        _apply=_errorFn2.apply(from, t);
      }
      _xblockexpression = _apply;
    }
    return (_xblockexpression).booleanValue();
  }
  
  public void onFinish(final int level) {
    Procedure1<Integer> _finishFn = this.getFinishFn();
    if (_finishFn!=null) {
      _finishFn.apply(Integer.valueOf(level));
    }
    if ((level == 0)) {
      Procedure0 _finish0Fn = this.getFinish0Fn();
      if (_finish0Fn!=null) {
        _finish0Fn.apply();
      }
    }
  }
  
  public void onClosed() {
    Procedure1<Void> _closedFn = this.getClosedFn();
    if (_closedFn!=null) {
      _closedFn.apply(null);
    }
  }
  
  private Procedure1<T> setValueFn(final Procedure1<T> value) {
    return this._valueFn.getAndSet(value);
  }
  
  private Procedure1<T> getValueFn() {
    return this._valueFn.get();
  }
  
  private Procedure2<R, T> setValueFn2(final Procedure2<R, T> value) {
    return this._valueFn2.getAndSet(value);
  }
  
  private Procedure2<R, T> getValueFn2() {
    return this._valueFn2.get();
  }
  
  private Function1<Throwable, Boolean> setErrorFn(final Function1<Throwable, Boolean> value) {
    return this._errorFn.getAndSet(value);
  }
  
  private Function1<Throwable, Boolean> getErrorFn() {
    return this._errorFn.get();
  }
  
  private Function2<R, Throwable, Boolean> setErrorFn2(final Function2<R, Throwable, Boolean> value) {
    return this._errorFn2.getAndSet(value);
  }
  
  private Function2<R, Throwable, Boolean> getErrorFn2() {
    return this._errorFn2.get();
  }
  
  private Procedure0 setFinish0Fn(final Procedure0 value) {
    return this._finish0Fn.getAndSet(value);
  }
  
  private Procedure0 getFinish0Fn() {
    return this._finish0Fn.get();
  }
  
  private Procedure1<Integer> setFinishFn(final Procedure1<Integer> value) {
    return this._finishFn.getAndSet(value);
  }
  
  private Procedure1<Integer> getFinishFn() {
    return this._finishFn.get();
  }
  
  private Procedure1<Void> setClosedFn(final Procedure1<Void> value) {
    return this._closedFn.getAndSet(value);
  }
  
  private Procedure1<Void> getClosedFn() {
    return this._closedFn.get();
  }
}
