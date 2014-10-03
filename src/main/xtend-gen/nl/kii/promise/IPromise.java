package nl.kii.promise;

import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public interface IPromise<T extends Object> /* extends Procedure1<Entry<T>>  */{
  public abstract Boolean getFulfilled();
  
  public abstract Entry<T> get();
  
  public abstract void set(final T value);
  
  public abstract IPromise<T> error(final Throwable t);
  
  public abstract IPromise<T> onError(final Procedure1<Throwable> errorFn);
  
  public abstract Task then(final Procedure1<T> valueFn);
  
  public abstract void setOperation(final String operation);
  
  public abstract String getOperation();
}
