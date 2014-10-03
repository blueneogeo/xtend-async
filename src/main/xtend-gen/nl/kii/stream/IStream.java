package nl.kii.stream;

import java.util.Collection;
import nl.kii.observe.Observable;
import nl.kii.stream.Entry;
import nl.kii.stream.StreamMessage;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * A sequence of elements supporting sequential and parallel aggregate operations.
 * <p>
 * It has the following features:
 * <ul>
 * <li>threadsafe
 * <li>all operations (including extensions) are non-blocking
 * <li>supports asynchronous processing through flow control (next)
 * <li>it is queued. you can optionally provide your own queue
 * <li>only allows a single listener (use a StreamObserver to listen with multiple listeners)
 * <li>supports aggregation through batches (data is separated through finish entries)
 * <li>supports multiple levels of aggregation through multiple batch/finish levels
 * <li>wraps errors and lets you listen for them at the end of the stream chain
 * </ul>
 */
@SuppressWarnings("all")
public interface IStream<R extends Object, T extends Object> extends Procedure1<StreamMessage>, Observable<Entry<R, T>> {
  public abstract void push(final R from, final T value);
  
  public abstract void error(final R from, final Throwable error);
  
  public abstract void finish();
  
  public abstract void finish(final int level);
  
  public abstract void next();
  
  public abstract void skip();
  
  public abstract void close();
  
  public abstract boolean isOpen();
  
  public abstract boolean isReady();
  
  public abstract boolean isSkipping();
  
  public abstract int getBufferSize();
  
  public abstract Collection<Entry<R, T>> getQueue();
  
  public abstract void setOperation(final String operationName);
  
  public abstract String getOperation();
}
