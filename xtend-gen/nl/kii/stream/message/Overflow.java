package nl.kii.stream.message;

import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamEvent;

/**
 * Warns that the buffer is full
 */
public class Overflow implements StreamEvent {
  public final /* Entry<? extends  */Object entry;
  
  public Overflow(final /* Entry<? extends  */Object entry) {
    this.entry = entry;
  }
}
