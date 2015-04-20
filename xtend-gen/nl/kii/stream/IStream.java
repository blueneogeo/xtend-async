package nl.kii.stream;

import nl.kii.observe.Observable;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamMessage;

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
public interface IStream<I extends java.lang.Object, O extends java.lang.Object> extends /* Procedure1<StreamMessage> */Observable<Entry<I, O>> {
  public abstract /* Object */Object apply(final StreamMessage message);
  
  public abstract void push(final I value);
  
  public abstract void error(final /* Throwable */Object error);
  
  public abstract void finish();
  
  public abstract void finish(final int level);
  
  public abstract void next();
  
  public abstract void skip();
  
  public abstract void close();
  
  public abstract /*  */Object onChange(final /*  */Object observeFn);
  
  public abstract /*  */Object onNotify(final /*  */Object notificationListener);
  
  public abstract IStream<I, I> getInput();
  
  public abstract boolean isOpen();
  
  public abstract boolean isReady();
  
  public abstract boolean isSkipping();
  
  public abstract /* Integer */Object getConcurrency();
  
  public abstract void setConcurrency(final /* Integer */Object concurrency);
  
  public abstract int getBufferSize();
  
  public abstract /* Collection<Entry<I, O>> */Object getQueue();
  
  public abstract void setOperation(final /* String */Object operationName);
  
  public abstract /* String */Object getOperation();
}
