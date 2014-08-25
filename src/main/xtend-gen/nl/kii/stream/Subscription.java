package nl.kii.stream;

import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.Task;
import nl.kii.stream.Closed;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class Subscription<T extends Object> implements Procedure1<Entry<T>> {
  protected final Stream<T> stream;
  
  private final Task task = new Task();
  
  @Atomic
  private final AtomicReference<Procedure1<Entry<T>>> _onEntryFn = new AtomicReference<Procedure1<Entry<T>>>();
  
  @Atomic
  private final AtomicReference<Procedure1<T>> _onValueFn = new AtomicReference<Procedure1<T>>();
  
  @Atomic
  private final AtomicReference<Procedure1<Throwable>> _onErrorFn = new AtomicReference<Procedure1<Throwable>>();
  
  @Atomic
  private final AtomicReference<Procedure0> _onFinish0Fn = new AtomicReference<Procedure0>();
  
  @Atomic
  private final AtomicReference<Procedure1<Finish<T>>> _onFinishFn = new AtomicReference<Procedure1<Finish<T>>>();
  
  @Atomic
  private final AtomicReference<Procedure0> _onClosedFn = new AtomicReference<Procedure0>();
  
  public Subscription(final Stream<T> stream) {
    this.stream = stream;
    final Procedure1<Entry<T>> _function = new Procedure1<Entry<T>>() {
      public void apply(final Entry<T> it) {
        Subscription.this.apply(it);
      }
    };
    stream.onChange(_function);
  }
  
  public void apply(final Entry<T> it) {
    Procedure1<Entry<T>> _onEntryFn = this.getOnEntryFn();
    if (_onEntryFn!=null) {
      _onEntryFn.apply(it);
    }
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof Value) {
        _matched=true;
        Procedure1<T> _onValueFn = this.getOnValueFn();
        if (_onValueFn!=null) {
          _onValueFn.apply(((Value<T>)it).value);
        }
      }
    }
    if (!_matched) {
      if (it instanceof nl.kii.stream.Error) {
        _matched=true;
        Procedure1<Throwable> _onErrorFn = this.getOnErrorFn();
        if (_onErrorFn!=null) {
          _onErrorFn.apply(((nl.kii.stream.Error<T>)it).error);
        }
        this.task.error(((nl.kii.stream.Error<T>)it).error);
      }
    }
    if (!_matched) {
      if (it instanceof Finish) {
        _matched=true;
        Procedure1<Finish<T>> _onFinishFn = this.getOnFinishFn();
        if (_onFinishFn!=null) {
          _onFinishFn.apply(((Finish<T>)it));
        }
        if ((((Finish<T>)it).level == 0)) {
          Procedure0 _onFinish0Fn = this.getOnFinish0Fn();
          if (_onFinish0Fn!=null) {
            _onFinish0Fn.apply();
          }
        }
        this.task.complete();
      }
    }
    if (!_matched) {
      if (it instanceof Closed) {
        _matched=true;
        Procedure0 _onClosedFn = this.getOnClosedFn();
        if (_onClosedFn!=null) {
          _onClosedFn.apply();
        }
        this.task.complete();
      }
    }
  }
  
  public Stream<T> getStream() {
    return this.stream;
  }
  
  public Procedure1<Entry<T>> entry(final Procedure1<? super Entry<T>> onEntryFn) {
    return this.setOnEntryFn(((Procedure1<Entry<T>>)onEntryFn));
  }
  
  public Procedure1<T> each(final Procedure1<? super T> onValueFn) {
    return this.setOnValueFn(((Procedure1<T>)onValueFn));
  }
  
  /**
   * listen for a finish (of level 0)
   */
  public Procedure0 finish(final Procedure0 onFinish0Fn) {
    return this.setOnFinish0Fn(onFinish0Fn);
  }
  
  /**
   * listen for any finish
   */
  public Procedure1<Finish<T>> finish(final Procedure1<? super Finish<T>> onFinishFn) {
    return this.setOnFinishFn(((Procedure1<Finish<T>>)onFinishFn));
  }
  
  public Procedure1<Throwable> error(final Procedure1<? super Throwable> onErrorFn) {
    return this.setOnErrorFn(((Procedure1<Throwable>)onErrorFn));
  }
  
  public Procedure0 closed(final Procedure0 onClosedFn) {
    return this.setOnClosedFn(onClosedFn);
  }
  
  public Task toTask() {
    return this.task;
  }
  
  private Procedure1<Entry<T>> setOnEntryFn(final Procedure1<Entry<T>> value) {
    return this._onEntryFn.getAndSet(value);
  }
  
  private Procedure1<Entry<T>> getOnEntryFn() {
    return this._onEntryFn.get();
  }
  
  private Procedure1<T> setOnValueFn(final Procedure1<T> value) {
    return this._onValueFn.getAndSet(value);
  }
  
  private Procedure1<T> getOnValueFn() {
    return this._onValueFn.get();
  }
  
  private Procedure1<Throwable> setOnErrorFn(final Procedure1<Throwable> value) {
    return this._onErrorFn.getAndSet(value);
  }
  
  private Procedure1<Throwable> getOnErrorFn() {
    return this._onErrorFn.get();
  }
  
  private Procedure0 setOnFinish0Fn(final Procedure0 value) {
    return this._onFinish0Fn.getAndSet(value);
  }
  
  private Procedure0 getOnFinish0Fn() {
    return this._onFinish0Fn.get();
  }
  
  private Procedure1<Finish<T>> setOnFinishFn(final Procedure1<Finish<T>> value) {
    return this._onFinishFn.getAndSet(value);
  }
  
  private Procedure1<Finish<T>> getOnFinishFn() {
    return this._onFinishFn.get();
  }
  
  private Procedure0 setOnClosedFn(final Procedure0 value) {
    return this._onClosedFn.getAndSet(value);
  }
  
  private Procedure0 getOnClosedFn() {
    return this._onClosedFn.get();
  }
}
