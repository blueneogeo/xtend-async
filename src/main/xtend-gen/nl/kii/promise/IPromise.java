package nl.kii.promise;

import nl.kii.promise.Task;
import nl.kii.stream.message.Entry;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public interface IPromise<R extends Object, T extends Object> extends Procedure1<Entry<R, T>> {
  public abstract IPromise<R, ?> getRoot();
  
  public abstract Boolean getFulfilled();
  
  public abstract Entry<R, T> get();
  
  public abstract void set(final R value);
  
  public abstract IPromise<R, T> error(final Throwable t);
  
  public abstract IPromise<R, T> onError(final Procedure1<Throwable> errorFn);
  
  public abstract IPromise<R, T> onError(final Procedure2<R, Throwable> errorFn);
  
  public abstract Task then(final Procedure1<T> valueFn);
  
  public abstract Task then(final Procedure2<R, T> valueFn);
  
  public abstract void setOperation(final String operation);
  
  public abstract String getOperation();
}
