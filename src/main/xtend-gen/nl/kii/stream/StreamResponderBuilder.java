package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Entry;
import nl.kii.stream.StreamMonitor;
import nl.kii.stream.StreamResponder;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class StreamResponderBuilder implements StreamMonitor, StreamResponder {
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
  
  public void onNext() {
    Procedure1<Void> _nextFn = this.getNextFn();
    if (_nextFn!=null) {
      _nextFn.apply(null);
    }
  }
  
  public void onSkip() {
    Procedure1<Void> _skipFn = this.getSkipFn();
    if (_skipFn!=null) {
      _skipFn.apply(null);
    }
  }
  
  public void onClose() {
    Procedure1<Void> _closeFn = this.getCloseFn();
    if (_closeFn!=null) {
      _closeFn.apply(null);
    }
  }
  
  public void onOverflow(final Entry<?, ?> entry) {
    Procedure1<Entry<?, ?>> _overflowFn = this.getOverflowFn();
    if (_overflowFn!=null) {
      _overflowFn.apply(entry);
    }
  }
  
  private Procedure1<Void> setNextFn(final Procedure1<Void> value) {
    return this._nextFn.getAndSet(value);
  }
  
  private Procedure1<Void> getNextFn() {
    return this._nextFn.get();
  }
  
  private Procedure1<Void> setSkipFn(final Procedure1<Void> value) {
    return this._skipFn.getAndSet(value);
  }
  
  private Procedure1<Void> getSkipFn() {
    return this._skipFn.get();
  }
  
  private Procedure1<Void> setCloseFn(final Procedure1<Void> value) {
    return this._closeFn.getAndSet(value);
  }
  
  private Procedure1<Void> getCloseFn() {
    return this._closeFn.get();
  }
  
  private Procedure1<Entry<?, ?>> setOverflowFn(final Procedure1<Entry<?, ?>> value) {
    return this._overflowFn.getAndSet(value);
  }
  
  private Procedure1<Entry<?, ?>> getOverflowFn() {
    return this._overflowFn.get();
  }
}
