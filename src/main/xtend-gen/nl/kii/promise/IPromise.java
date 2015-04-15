package nl.kii.promise;

import nl.kii.promise.Task;
import nl.kii.stream.message.Entry;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public interface IPromise<I extends Object, O extends Object> extends Procedure1<Entry<I, O>> {
  public abstract IPromise<I, ?> getRoot();
  
  public abstract Boolean getFulfilled();
  
  public abstract Entry<I, O> get();
  
  public abstract void set(final I value);
  
  public abstract IPromise<I, O> error(final Throwable t);
  
  public abstract IPromise<I, O> on(final Class<? extends Throwable> exceptionType, final Procedure1<Throwable> errorFn);
  
  public abstract IPromise<I, O> on(final Class<? extends Throwable> exceptionType, final Procedure2<I, Throwable> errorFn);
  
  public abstract Task then(final Procedure1<O> valueFn);
  
  public abstract Task then(final Procedure2<I, O> valueFn);
  
  public abstract void setOperation(final String operation);
  
  public abstract String getOperation();
}
