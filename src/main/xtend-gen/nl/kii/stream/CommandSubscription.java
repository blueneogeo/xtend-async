package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.stream.Close;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamCommand;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class CommandSubscription implements Procedure1<StreamCommand> {
  private final Stream<?> stream;
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _onNextFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _onSkipFn = new AtomicReference<Procedure1<Void>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Void>> _onCloseFn = new AtomicReference<Procedure1<Void>>();
  
  public CommandSubscription(final Stream<?> stream) {
    this.stream = stream;
    final Procedure1<StreamCommand> _function = new Procedure1<StreamCommand>() {
      public void apply(final StreamCommand it) {
        CommandSubscription.this.apply(it);
      }
    };
    stream.onNotification(_function);
  }
  
  public void apply(final StreamCommand it) {
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Next) {
        _matched=true;
        Procedure1<Void> _onNextFn = this.getOnNextFn();
        if (_onNextFn!=null) {
          _onNextFn.apply(null);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Skip) {
        _matched=true;
        Procedure1<Void> _onSkipFn = this.getOnSkipFn();
        if (_onSkipFn!=null) {
          _onSkipFn.apply(null);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Close) {
        _matched=true;
        Procedure1<Void> _onCloseFn = this.getOnCloseFn();
        if (_onCloseFn!=null) {
          _onCloseFn.apply(null);
        }
      }
    }
  }
  
  public Procedure1<Void> onNext(final Procedure1<? super Void> onNextFn) {
    return this.setOnNextFn(((Procedure1<Void>)onNextFn));
  }
  
  public Procedure1<Void> onSkip(final Procedure1<? super Void> onSkipFn) {
    return this.setOnSkipFn(((Procedure1<Void>)onSkipFn));
  }
  
  public Procedure1<Void> onClose(final Procedure1<? super Void> onCloseFn) {
    return this.setOnCloseFn(((Procedure1<Void>)onCloseFn));
  }
  
  private Procedure1<Void> setOnNextFn(final Procedure1<Void> value) {
    return this._onNextFn.getAndSet(value);
  }
  
  private Procedure1<Void> getOnNextFn() {
    return this._onNextFn.get();
  }
  
  private Procedure1<Void> setOnSkipFn(final Procedure1<Void> value) {
    return this._onSkipFn.getAndSet(value);
  }
  
  private Procedure1<Void> getOnSkipFn() {
    return this._onSkipFn.get();
  }
  
  private Procedure1<Void> setOnCloseFn(final Procedure1<Void> value) {
    return this._onCloseFn.getAndSet(value);
  }
  
  private Procedure1<Void> getOnCloseFn() {
    return this._onCloseFn.get();
  }
}
