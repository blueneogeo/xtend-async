package nl.kii.stream;

import nl.kii.stream.Close;
import nl.kii.stream.Next;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamCommand;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class CommandSubscription implements Procedure1<StreamCommand> {
  private final Stream<?> stream;
  
  private Procedure1<? super Void> onNextFn;
  
  private Procedure1<? super Void> onSkipFn;
  
  private Procedure1<? super Void> onCloseFn;
  
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
        if (this.onNextFn!=null) {
          this.onNextFn.apply(null);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Skip) {
        _matched=true;
        if (this.onSkipFn!=null) {
          this.onSkipFn.apply(null);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Close) {
        _matched=true;
        if (this.onCloseFn!=null) {
          this.onCloseFn.apply(null);
        }
      }
    }
  }
  
  public Procedure1<? super Void> onNext(final Procedure1<? super Void> onNextFn) {
    return this.onNextFn = onNextFn;
  }
  
  public Procedure1<? super Void> onSkip(final Procedure1<? super Void> onSkipFn) {
    return this.onSkipFn = onSkipFn;
  }
  
  public Procedure1<? super Void> onClose(final Procedure1<? super Void> onCloseFn) {
    return this.onCloseFn = onCloseFn;
  }
}
