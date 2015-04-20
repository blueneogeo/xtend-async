package nl.kii.promise;

import nl.kii.promise.Task;
import nl.kii.stream.message.Entry;

public interface IPromise<I extends java.lang.Object, O extends java.lang.Object> /* extends Procedure1<Entry<I, O>>  */{
  public abstract /* IPromise<I, ? extends  */Object getInput();
  
  public abstract /* Boolean */Object getFulfilled();
  
  public abstract Entry<I, O> get();
  
  public abstract void set(final I value);
  
  public abstract IPromise<I, O> error(final /* Throwable */Object t);
  
  public abstract IPromise<I, O> on(final /* Class<? extends Throwable> */Object exceptionType, final boolean swallow, final /* Procedure2<I, Throwable> */Object errorFn);
  
  public abstract Task then(final /* Procedure1<O> */Object valueFn);
  
  public abstract Task then(final /* Procedure2<I, O> */Object valueFn);
  
  public abstract void setOperation(final /* String */Object operation);
  
  public abstract /* String */Object getOperation();
}
