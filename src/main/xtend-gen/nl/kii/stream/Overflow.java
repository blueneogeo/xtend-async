package nl.kii.stream;

import nl.kii.stream.Entry;
import nl.kii.stream.StreamNotification;

/**
 * Warns that the buffer is full
 */
@SuppressWarnings("all")
public class Overflow implements StreamNotification {
  public final Entry<?> entry;
  
  public Overflow(final Entry<?> entry) {
    this.entry = entry;
  }
}
