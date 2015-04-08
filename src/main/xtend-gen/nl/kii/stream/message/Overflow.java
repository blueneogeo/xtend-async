package nl.kii.stream.message;

import nl.kii.stream.message.Entry;
import nl.kii.stream.message.StreamEvent;

/**
 * Warns that the buffer is full
 */
@SuppressWarnings("all")
public class Overflow implements StreamEvent {
  public final Entry<?, ?> entry;
  
  public Overflow(final Entry<?, ?> entry) {
    this.entry = entry;
  }
}
