package nl.kii.stream;

import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Lets you create builders for handling the entries coming from a stream
 */
@SuppressWarnings("all")
public interface StreamHandler<T extends Object> extends Procedure1<Entry<T>> {
  /**
   * handle each incoming value. remember to call stream.next after handling a value!
   */
  public abstract void each(final Procedure1<? super T> handler);
  
  /**
   * handle each incoming error. remember to call stream.next after handling an error!
   */
  public abstract void error(final Procedure1<? super Throwable> handler);
  
  /**
   * handle each incoming finish. remember to call stream.next after handling a finish!
   */
  public abstract void finish(final Procedure1<? super Finish<T>> handler);
  
  /**
   * handled that the stream has closed.
   */
  public abstract void closed(final Procedure1<? super Void> stream);
}
