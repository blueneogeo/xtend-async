package nl.kii.promise;

import nl.kii.promise.Promise;
import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public interface IPromise<T extends Object> extends Procedure1<Entry<T>> {
  public abstract Boolean getFulfilled();
  
  public abstract Entry<T> get();
  
  public abstract Promise<T> set(final T value);
  
  public abstract Promise<T> error(final Throwable t);
  
  public abstract Promise<T> onError(final Procedure1<Throwable> errorFn);
  
  public abstract Promise<T> always(final Procedure1<Entry<T>> resultFn);
  
  public abstract Task then(final Procedure1<T> valueFn);
}
