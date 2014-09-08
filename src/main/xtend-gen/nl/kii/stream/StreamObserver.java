package nl.kii.stream;

import nl.kii.promise.Task;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public interface StreamObserver<T extends Object> extends Procedure1<Entry<T>> {
  /**
   * handle each incoming value
   */
  public abstract void each(final Procedure1<? super T> handler);
  
  /**
   * handle each incoming error
   */
  public abstract void error(final Procedure1<? super Throwable> handler);
  
  /**
   * handle each incoming finish
   */
  public abstract void finish(final Procedure1<? super Finish<T>> handler);
  
  /**
   * handled that the stream has closed
   */
  public abstract void closed(final Procedure1<? super Void> stream);
  
  public abstract Stream<T> getStream();
  
  public abstract Task toTask();
}
