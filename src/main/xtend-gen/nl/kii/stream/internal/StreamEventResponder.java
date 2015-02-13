package nl.kii.stream.internal;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.internal.StreamEventHandler;
import nl.kii.stream.message.Entry;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class StreamEventResponder implements StreamEventHandler {
  @Atomic
  private final AtomicReference<Procedure1<Void>> _nextFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _skipFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _closeFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Entry<?, ?>>> _overflowFn = new AtomicReference<Procedure1<Entry<?, ?>>>();
  
  public void next(final Procedure1<? super Void> handler) {
    this.setNextFn(((Procedure1<Void>)handler));
  }
  
  public void skip(final Procedure1<? super Void> handler) {
    this.setSkipFn(((Procedure1<Void>)handler));
  }
  
  public void close(final Procedure1<? super Void> handler) {
    this.setCloseFn(((Procedure1<Void>)handler));
  }
  
  public void overflow(final Procedure1<? super Entry<?, ?>> handler) {
    this.setOverflowFn(((Procedure1<Entry<?, ?>>)handler));
  }
  
  @Override
  public void onNext() {
    Procedure1<Void> _nextFn = this.getNextFn();
    if (_nextFn!=null) {
      _nextFn.apply(null);
    }
  }
  
  @Override
  public void onSkip() {
    Procedure1<Void> _skipFn = this.getSkipFn();
    if (_skipFn!=null) {
      _skipFn.apply(null);
    }
  }
  
  @Override
  public void onClose() {
    Procedure1<Void> _closeFn = this.getCloseFn();
    if (_closeFn!=null) {
      _closeFn.apply(null);
    }
  }
  
  @Override
  public void onOverflow(final Entry<?, ?> entry) {
    Procedure1<Entry<?, ?>> _overflowFn = this.getOverflowFn();
    if (_overflowFn!=null) {
      _overflowFn.apply(entry);
    }
  }
  
  private void setNextFn(final Procedure1<Void> value) {
    this._nextFn.set(value);
  }
  
  private Procedure1<Void> getNextFn() {
    return this._nextFn.get();
  }
  
  private Procedure1<Void> getAndSetNextFn(final Procedure1<Void> value) {
    return this._nextFn.getAndSet(value);
  }
  
  private void setSkipFn(final Procedure1<Void> value) {
    this._skipFn.set(value);
  }
  
  private Procedure1<Void> getSkipFn() {
    return this._skipFn.get();
  }
  
  private Procedure1<Void> getAndSetSkipFn(final Procedure1<Void> value) {
    return this._skipFn.getAndSet(value);
  }
  
  private void setCloseFn(final Procedure1<Void> value) {
    this._closeFn.set(value);
  }
  
  private Procedure1<Void> getCloseFn() {
    return this._closeFn.get();
  }
  
  private Procedure1<Void> getAndSetCloseFn(final Procedure1<Void> value) {
    return this._closeFn.getAndSet(value);
  }
  
  private void setOverflowFn(final Procedure1<Entry<?, ?>> value) {
    this._overflowFn.set(value);
  }
  
  private Procedure1<Entry<?, ?>> getOverflowFn() {
    return this._overflowFn.get();
  }
  
  private Procedure1<Entry<?, ?>> getAndSetOverflowFn(final Procedure1<Entry<?, ?>> value) {
    return this._overflowFn.getAndSet(value);
  }
}
