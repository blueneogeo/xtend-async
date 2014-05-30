package nl.kii.stream;

import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class Subscription<T extends Object> implements Procedure1<Entry<T>> {
  protected final Stream<T> stream;
  
  private Procedure1<? super T> onValueFn;
  
  private Procedure1<? super Throwable> onErrorFn;
  
  private Procedure1<? super Void> onFinishFn;
  
  public Subscription(final Stream<T> stream) {
    this.stream = stream;
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        Subscription.this.apply(it);
      }
    };
    stream.onEntry(_function);
  }
  
  public void apply(final Entry<T> it) {
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Value) {
        _matched=true;
        if (this.onValueFn!=null) {
          this.onValueFn.apply(((Value<T>)it).value);
        }
      }
    }
    if (!_matched) {
      if (it instanceof nl.kii.stream.Error) {
        _matched=true;
        if (this.onErrorFn!=null) {
          this.onErrorFn.apply(((nl.kii.stream.Error<T>)it).error);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Finish) {
        _matched=true;
        if (this.onFinishFn!=null) {
          this.onFinishFn.apply(null);
        }
      }
    }
  }
  
  public Procedure1<? super T> each(final Procedure1<? super T> onValueFn) {
    return this.onValueFn = onValueFn;
  }
  
  public Procedure1<? super Void> finish(final Procedure1<? super Void> onFinishFn) {
    return this.onFinishFn = onFinishFn;
  }
  
  public Procedure1<? super Throwable> error(final Procedure1<? super Throwable> onErrorFn) {
    return this.onErrorFn = onErrorFn;
  }
  
  public void close() {
    this.stream.close();
  }
}
