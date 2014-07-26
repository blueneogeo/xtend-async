package nl.kii.stream;

import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.Subscription;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class AsyncSubscription<T extends Object> extends Subscription<T> {
  private final Procedure1<Object> nextFn = new Procedure1<Object>() {
    public void apply(final Object it) {
      AsyncSubscription.this.next();
    }
  };
  
  protected Procedure1<? super Entry<T>> onEntryFn = this.nextFn;
  
  protected Procedure1<? super T> onValueFn = this.nextFn;
  
  protected Procedure1<? super Throwable> onErrorFn = this.nextFn;
  
  protected Procedure0 onFinish0Fn = new Procedure0() {
    public void apply() {
      AsyncSubscription.this.next();
    }
  };
  
  protected Procedure1<? super Finish<T>> onFinishFn = this.nextFn;
  
  public AsyncSubscription(final Stream<T> stream) {
    super(stream);
  }
  
  public void next() {
    this.stream.next();
  }
  
  public void skip() {
    this.stream.skip();
  }
  
  public void close() {
    this.stream.close();
  }
}
