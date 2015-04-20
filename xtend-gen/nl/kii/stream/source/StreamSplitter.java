package nl.kii.stream.source;

import nl.kii.act.Actor;
import nl.kii.stream.IStream;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.message.StreamMessage;
import nl.kii.stream.source.StreamSource;

/**
 * A splitter takes a stream and lets you use it as a source
 * for other streams. It usually implements a specific value
 * distribution system.
 */
public abstract class StreamSplitter<I extends java.lang.Object, O extends java.lang.Object> extends Actor<StreamMessage> implements StreamSource<I, O> {
  /**
   * the source stream that gets distributed
   */
  protected final IStream<I, O> source;
  
  /**
   * the connected listening streams
   */
  /* @Atomic
   */protected final /* List<IStream<I, ? extends  */Object streams;
  
  public StreamSplitter(final IStream<I, O> source) {
    throw new Error("Unresolved compilation problems:"
      + "\nCopyOnWriteArrayList cannot be resolved."
      + "\nType mismatch: cannot convert implicit first argument from StreamSplitter<I, O> to StreamMessage");
  }
  
  public StreamSource<I, O> pipe(final /* IStream<I, ? extends  */Object stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nType mismatch: cannot convert implicit first argument from StreamSplitter<I, O> to StreamMessage"
      + "\n+= cannot be resolved");
  }
  
  public IStream<I, O> stream() {
    throw new Error("Unresolved compilation problems:"
      + "\n=> cannot be resolved."
      + "\nType mismatch: cannot convert implicit first argument from StreamSplitter<I, O> to IStream<I, ? extends Object>");
  }
  
  /**
   * we are wrapping in an actor to make things threadsafe
   */
  protected void act(final StreamMessage message, final /*  */Object done) {
    throw new Error("Unresolved compilation problems:"
      + "\napply cannot be resolved");
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected abstract void onEntry(final Entry<I, O> entry);
  
  /**
   * Handle a message coming from a piped stream
   */
  protected abstract void onCommand(final StreamEvent msg);
  
  /**
   * Utility method that only returns true if all members match the condition
   */
  protected static <T extends java.lang.Object> boolean all(final /* Iterable<T> */Object list, final /*  */Object conditionFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nfindFirst cannot be resolved"
      + "\napply cannot be resolved"
      + "\n! cannot be resolved"
      + "\n== cannot be resolved");
  }
}
