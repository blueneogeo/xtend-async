package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.IStream;
import nl.kii.stream.StreamHandler;
import nl.kii.stream.StreamObserver;
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
  private final AtomicReference<Procedure2<R, T>> _valueFn = new AtomicReference<Procedure2<R, T>>();
  
  @Atomic
  private final AtomicReference<Procedure2<R, Throwable>> _errorFn = new AtomicReference<Procedure2<R, Throwable>>();
  
  @Atomic
  private final AtomicReference<Procedure2<R, Integer>> _finishFn = new AtomicReference<Procedure2<R, Integer>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _closedFn = new AtomicReference<Procedure1<Void>>();
  
  public StreamHandlerBuilder(final IStream<R, T> stream) {
    this.stream = stream;
  }
  
  /**
   * listen for each incoming value
   */
  public void each(final Procedure2<? super R, ? super T> handler) {
    this.setValueFn(((Procedure2<R, T>)handler));
  }
  
  /**
   * listen for any finish
   */
  public void finish(final Procedure2<? super R, ? super Integer> handler) {
    this.setFinishFn(((Procedure2<R, Integer>)handler));
  }
  
  /**
   * listen for any uncaught errors
   */
  public void error(final Procedure2<? super R, ? super Throwable> handler) {
    this.setErrorFn(((Procedure2<R, Throwable>)handler));
  }
  
  /**
   * listen for when the stream closes
   */
  public void closed(final Procedure1<? super Void> handler) {
    this.setClosedFn(((Procedure1<Void>)handler));
  }
  
  public void onValue(final R from, final T value) {
    Procedure2<R, T> _valueFn = this.getValueFn();
    if (_valueFn!=null) {
      _valueFn.apply(from, value);
    }
  }
  
  public void onError(final R from, final Throwable t) {
    Procedure2<R, Throwable> _errorFn = this.getErrorFn();
    if (_errorFn!=null) {
      _errorFn.apply(from, t);
    }
  }
  
  public void onFinish(final R from, final int level) {
    Procedure2<R, Integer> _finishFn = this.getFinishFn();
    if (_finishFn!=null) {
      _finishFn.apply(from, Integer.valueOf(level));
    }
  }
  
  public void onClosed() {
    Procedure1<Void> _closedFn = this.getClosedFn();
    if (_closedFn!=null) {
      _closedFn.apply(null);
    }
  }
  
  private Procedure2<R, T> setValueFn(final Procedure2<R, T> value) {
    return this._valueFn.getAndSet(value);
  }
  
  private Procedure2<R, T> getValueFn() {
    return this._valueFn.get();
  }
  
  private Procedure2<R, Throwable> setErrorFn(final Procedure2<R, Throwable> value) {
    return this._errorFn.getAndSet(value);
  }
  
  private Procedure2<R, Throwable> getErrorFn() {
    return this._errorFn.get();
  }
  
  private Procedure2<R, Integer> setFinishFn(final Procedure2<R, Integer> value) {
    return this._finishFn.getAndSet(value);
  }
  
  private Procedure2<R, Integer> getFinishFn() {
    return this._finishFn.get();
  }
  
  private Procedure1<Void> setClosedFn(final Procedure1<Void> value) {
    return this._closedFn.getAndSet(value);
  }
  
  private Procedure1<Void> getClosedFn() {
    return this._closedFn.get();
  }
}
