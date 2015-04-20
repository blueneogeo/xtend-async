package nl.kii.stream.message;

import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamMessage;

/**
 * Use entries to push multiple entries onto the stream for one recieved entry.
 * Consider it an atomic push of multiple entries onto the stream.
 */
public class Entries<I extends java.lang.Object, O extends java.lang.Object> implements StreamMessage {
  public final /* List<Entry<I, O>> */Object entries;
  
  public Entries(final Entry<I, O>... entries) {
    this.entries = entries;
  }
  
  public Object toString() {
    throw new Error("Unresolved compilation problems:"
      + "\ntoString cannot be resolved");
  }
  
  public Object equals(final /* Object */Object o) {
    throw new Error("Unresolved compilation problems:"
      + "\n&& cannot be resolved."
      + "\n== cannot be resolved");
  }
}
