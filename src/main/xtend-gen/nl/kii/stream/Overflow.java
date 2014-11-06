package nl.kii.stream;

import nl.kii.stream.Entry;
import nl.kii.stream.StreamEvent;

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
