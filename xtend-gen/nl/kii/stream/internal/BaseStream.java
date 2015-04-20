package nl.kii.stream.internal;

import nl.kii.act.Actor;
import nl.kii.stream.IStream;
import nl.kii.stream.message.Close;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Next;
import nl.kii.stream.message.Skip;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.message.StreamMessage;

public abstract class BaseStream<I extends java.lang.Object, O extends java.lang.Object> extends Actor<StreamMessage> implements IStream<I, O> {
  public final static int DEFAULT_MAX_BUFFERSIZE = 1000;
  
  protected final /* Queue<Entry<I, O>> */Object queue;
  
  /* @Atomic
   */private final int buffersize = 0;
  
  /* @Atomic
   */private final boolean open = true;
  
  /* @Atomic
   */private final boolean ready = false;
  
  /* @Atomic
   */private final boolean skipping = false;
  
  /* @Atomic
   */private final /*  */Object entryListener;
  
  /* @Atomic
   */private final /*  */Object notificationListener;
  
  /* @Atomic
   */public final int concurrency = 0;
  
  /* @Atomic
   */public final int maxBufferSize;
  
  /* @Atomic
   */public final /* String */Object operation;
  
  /**
   * Create the stream with a memory concurrent queue
   */
  public BaseStream() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field newConcurrentLinkedQueue is undefined for the type BaseStream");
  }
  
  public BaseStream(final int maxBufferSize) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field newConcurrentLinkedQueue is undefined for the type BaseStream");
  }
  
  /**
   * Create the stream with your own provided queue.
   * Note: the queue must be threadsafe for streams to be threadsafe!
   */
  public BaseStream(final /* Queue<Entry<I, O>> */Object queue, final int maxBufferSize) {
    this.queue = queue;
    this.maxBufferSize = maxBufferSize;
    this.operation = "source";
  }
  
  /**
   * Get the queue of the stream. will only be an unmodifiable view of the queue.
   */
  public Collection getQueue() {
    throw new Error("Unresolved compilation problems:"
      + "\nunmodifiableView cannot be resolved");
  }
  
  public boolean isOpen() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field getOpen is undefined for the type BaseStream");
  }
  
  public boolean isReady() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field getReady is undefined for the type BaseStream");
  }
  
  public boolean isSkipping() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field getSkipping is undefined for the type BaseStream");
  }
  
  public int getBufferSize() {
    return this.buffersize;
  }
  
  /**
   * Ask for the next value in the buffer to be delivered to the change listener
   */
  public void next() {
    Next _next = new Next();
    this.apply(_next);
  }
  
  /**
   * Tell the stream to stop sending values until the next Finish(0)
   */
  public void skip() {
    Skip _skip = new Skip();
    this.apply(_skip);
  }
  
  /**
   * Close the stream, which will stop the listener from recieving values
   */
  public void close() {
    Close _close = new Close();
    this.apply(_close);
  }
  
  /**
   * Listen for changes on the stream. There can only be a single change listener.
   * <p>
   * this is used mostly internally, and you are encouraged to use .observe() or
   * the StreamExtensions instead.
   * @return unsubscribe function
   */
  public /*  */Object onChange(final /*  */Object entryListener) {
    throw new Error("Unresolved compilation problems:"
      + "\nAssignment to final field"
      + "\nAssignment to final field");
  }
  
  /**
   * Listen for notifications from the stream.
   * <p>
   * this is used mostly internally, and you are encouraged to use .monitor() or
   * the StreamExtensions instead.
   * @return unsubscribe function
   */
  public /*  */Object onNotify(final /*  */Object notificationListener) {
    throw new Error("Unresolved compilation problems:"
      + "\nAssignment to final field"
      + "\nAssignment to final field");
  }
  
  /**
   * Process a single incoming stream message from the actor queue.
   */
  protected void act(final StreamMessage entry, final /*  */Object done) {
    throw new Error("Unresolved compilation problems:"
      + "\n>= cannot be resolved."
      + "\nThe method or field incBuffersize is undefined for the type BaseStream"
      + "\nThe method incBuffersize is undefined for the type BaseStream"
      + "\n! cannot be resolved."
      + "\n&& cannot be resolved."
      + "\nThe method or field level is undefined for the type BaseStream"
      + "\nThe method or field decBuffersize is undefined for the type BaseStream"
      + "\nThe method or field incBuffersize is undefined for the type BaseStream"
      + "\nAssignment to final field"
      + "\nAssignment to final field"
      + "\nAssignment to final field"
      + "\nAssignment to final field"
      + "\nAssignment to final field"
      + "\napply cannot be resolved"
      + "\nadd cannot be resolved"
      + "\naddAll cannot be resolved"
      + "\nsize cannot be resolved"
      + "\nempty cannot be resolved"
      + "\n! cannot be resolved"
      + "\npeek cannot be resolved"
      + "\n== cannot be resolved"
      + "\npoll cannot be resolved"
      + "\nadd cannot be resolved"
      + "\nclear cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  /**
   * Publish a single entry from the stream queue.
   */
  protected boolean publishNext() {
    throw new Error("Unresolved compilation problems:"
      + "\nThrowable cannot be resolved to a type."
      + "\n! cannot be resolved."
      + "\n! cannot be resolved."
      + "\nThe method or field decBuffersize is undefined for the type BaseStream"
      + "\nThe method or field level is undefined for the type BaseStream"
      + "\n! cannot be resolved."
      + "\nAssignment to final field"
      + "\nUnreachable code: The catch block can never match. It is already handled by a previous condition."
      + "\nAssignment to final field"
      + "\nNo exception of type UncaughtStreamException can be thrown; an exception type must be a subclass of Throwable"
      + "\nNo exception of type Throwable can be thrown; an exception type must be a subclass of Throwable"
      + "\nAssignment to final field"
      + "\n|| cannot be resolved"
      + "\n|| cannot be resolved"
      + "\n== cannot be resolved"
      + "\n|| cannot be resolved"
      + "\nempty cannot be resolved"
      + "\npoll cannot be resolved"
      + "\n== cannot be resolved"
      + "\napply cannot be resolved"
      + "\nfrom cannot be resolved"
      + "\nfrom cannot be resolved");
  }
  
  /**
   * helper function for informing the notify listener
   */
  protected Object notify(final StreamEvent command) {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved"
      + "\napply cannot be resolved");
  }
  
  public java.lang.CharSequence toString() {
    throw new Error("Unresolved compilation problems:"
      + "\nsize cannot be resolved"
      + "\n!= cannot be resolved");
  }
}
