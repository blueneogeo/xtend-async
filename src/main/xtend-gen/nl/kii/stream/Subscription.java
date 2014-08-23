package nl.kii.stream;

import java.util.concurrent.Executor;
import nl.kii.async.annotation.Async;
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
  
  protected Task task;
  
  protected Procedure1<? super Entry<T>> onEntryFn;
  
  protected Procedure1<? super T> onValueFn;
  
  protected Procedure1<? super Throwable> onErrorFn;
  
  protected Procedure0 onFinish0Fn;
  
  protected Procedure1<? super Finish<T>> onFinishFn;
  
  protected Procedure0 onClosedFn;
  
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
    if (this.onEntryFn!=null) {
      this.onEntryFn.apply(it);
    }
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
        if (this.task!=null) {
          this.task.error(((nl.kii.stream.Error<T>)it).error);
        }
      }
    }
    if (!_matched) {
      if (it instanceof Finish) {
        _matched=true;
        if (this.onFinishFn!=null) {
          this.onFinishFn.apply(((Finish<T>)it));
        }
        if ((((Finish<T>)it).level == 0)) {
          if (this.onFinish0Fn!=null) {
            this.onFinish0Fn.apply();
          }
        }
        if (this.task!=null) {
          this.task.complete();
        }
      }
    }
    if (!_matched) {
      if (it instanceof Closed) {
        _matched=true;
        if (this.onClosedFn!=null) {
          this.onClosedFn.apply();
        }
      }
    }
  }
  
  public Stream<T> getStream() {
    return this.stream;
  }
  
  public Procedure1<? super Entry<T>> entry(final Procedure1<? super Entry<T>> onEntryFn) {
    return this.onEntryFn = onEntryFn;
  }
  
  public Procedure1<? super T> each(final Procedure1<? super T> onValueFn) {
    return this.onValueFn = onValueFn;
  }
  
  /**
   * listen for a finish (of level 0)
   */
  public Procedure0 finish(final Procedure0 onFinish0Fn) {
    return this.onFinish0Fn = onFinish0Fn;
  }
  
  /**
   * listen for any finish
   */
  public Procedure1<? super Finish<T>> finish(final Procedure1<? super Finish<T>> onFinishFn) {
    return this.onFinishFn = onFinishFn;
  }
  
  public Procedure1<? super Throwable> error(final Procedure1<? super Throwable> onErrorFn) {
    return this.onErrorFn = onErrorFn;
  }
  
  public Procedure0 closed(final Procedure0 onClosedFn) {
    return this.onClosedFn = onClosedFn;
  }
  
  @Async
  public Task toTask(final Task task) {
    return this.task = task;
  }
  
  public Task toTask() {
    final Task task = new Task();
    try {
    	toTask(task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  public Task toTask(final Executor executor) {
    final Task task = new Task();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			toTask(task);
    		} catch(Throwable t) {
    			task.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return task;
  }
}
