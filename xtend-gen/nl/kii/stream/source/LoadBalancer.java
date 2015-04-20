package nl.kii.stream.source;

import nl.kii.stream.IStream;
import nl.kii.stream.message.Close;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Next;
import nl.kii.stream.message.Skip;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.source.StreamSplitter;

/**
 * This splitter sends each message to the first stream that is ready.
 * This means that each attached stream receives different messages.
 */
public class LoadBalancer<I extends java.lang.Object, O extends java.lang.Object> extends StreamSplitter<I, O> {
  public LoadBalancer(final IStream<I, O> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<I, O> entry) {
    throw new Error("Unresolved compilation problems:"
      + "\nready cannot be resolved"
      + "\napply cannot be resolved"
      + "\nfinish cannot be resolved"
      + "\nerror cannot be resolved");
  }
  
  protected void onCommand(final StreamEvent msg) {
    boolean _matched = false;
    if (!_matched) {
      if (msg instanceof Next) {
        _matched=true;
        this.next();
      }
    }
    if (!_matched) {
      if (msg instanceof Skip) {
        _matched=true;
        this.skip();
      }
    }
    if (!_matched) {
      if (msg instanceof Close) {
        _matched=true;
        this.close();
      }
    }
  }
  
  protected void next() {
    this.source.next();
  }
  
  protected void skip() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field skipping is undefined for the type LoadBalancer"
      + "\nall cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  protected void close() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field open is undefined for the type LoadBalancer"
      + "\nall cannot be resolved"
      + "\n! cannot be resolved"
      + "\n! cannot be resolved");
  }
}
