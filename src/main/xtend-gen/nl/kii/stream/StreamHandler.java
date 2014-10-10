package nl.kii.stream;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * Lets you create builders for handling the entries coming from a stream
 */
@SuppressWarnings("all")
public interface StreamHandler<R extends Object, T extends Object> {
  /**
   * handle each incoming value. remember to call stream.next after handling a value!
   */
  public abstract void each(final Procedure2<? super R, ? super T> handler);
  
  /**
   * handle each incoming error. remember to call stream.next after handling an error!
   */
  public abstract void error(final Procedure2<? super R, ? super Throwable> handler);
  
  /**
   * handle each incoming finish. remember to call stream.next after handling a finish!
   */
  public abstract void finish(final Procedure2<? super R, ? super Integer> handler);
  
  /**
   * handled that the stream has closed.
   */
  public abstract void closed(final Procedure1<? super Void> stream);
}
