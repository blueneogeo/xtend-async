package nl.kii.stream.source;

import nl.kii.stream.IStream;
import nl.kii.stream.message.Close;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Next;
import nl.kii.stream.message.Skip;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.source.StreamSplitter;

/**
 * This splitter simply tries to pass all incoming values
 * from the source stream to the piped streams.
 * <p>
 * Flow control is maintained by only allowing the next
 * entry from the source stream if all piped streams are ready.
 * <p>
 * Note: This means that you need to make sure that all connected
 * streams do not block their flow, since one blocking stream
 * will block all streams from flowing.
 */
public class StreamCopySplitter<I extends java.lang.Object, O extends java.lang.Object> extends StreamSplitter<I, O> {
  /* @Atomic
   */private Entry<I, O> buffer;
  
  public StreamCopySplitter(final IStream<I, O> source) {
    super(source);
  }
  
  /**
   * Handle an entry coming in from the source stream
   */
  protected void onEntry(final Entry<I, O> entry) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field ready is undefined for the type StreamCopySplitter"
      + "\nall cannot be resolved");
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
  
  protected Entry<I, O> publish() {
    throw new Error("Unresolved compilation problems:"
      + "\n!= cannot be resolved."
      + "\napply cannot be resolved");
  }
  
  protected void next() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field ready is undefined for the type StreamCopySplitter"
      + "\nall cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  protected void skip() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field skipping is undefined for the type StreamCopySplitter"
      + "\nall cannot be resolved"
      + "\n! cannot be resolved");
  }
  
  protected void close() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field open is undefined for the type StreamCopySplitter"
      + "\nall cannot be resolved"
      + "\n! cannot be resolved"
      + "\n! cannot be resolved");
  }
}
