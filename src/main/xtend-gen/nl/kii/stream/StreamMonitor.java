package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Close;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.StreamNotificationObserver;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class StreamMonitor implements StreamNotificationObserver {
  private final Stream<?> stream;
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _nextFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _skipFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _closeFn = new AtomicReference<Procedure1<Void>>();
  
  public StreamMonitor(final Stream<?> stream) {
    this.stream = stream;
    final Procedure1<StreamNotification> _function = new Procedure1<StreamNotification>() {
      public void apply(final StreamNotification it) {
        StreamMonitor.this.apply(it);
      }
    };
    stream.onNotification(_function);
  }
  
  public void apply(final StreamNotification it) {
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Next) {
        _matched=true;
        Procedure1<Void> _nextFn = this.getNextFn();
        if (_nextFn!=null) {
          _nextFn.apply(null);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Skip) {
        _matched=true;
        Procedure1<Void> _skipFn = this.getSkipFn();
        if (_skipFn!=null) {
          _skipFn.apply(null);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Close) {
        _matched=true;
        Procedure1<Void> _closeFn = this.getCloseFn();
        if (_closeFn!=null) {
          _closeFn.apply(null);
        }
      }
    }
  }
  
  public void next(final Procedure1<? super Void> handler) {
    this.setNextFn(((Procedure1<Void>)handler));
  }
  
  public void skip(final Procedure1<? super Void> handler) {
    this.setSkipFn(((Procedure1<Void>)handler));
  }
  
  public void close(final Procedure1<? super Void> handler) {
    this.setCloseFn(((Procedure1<Void>)handler));
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
}
