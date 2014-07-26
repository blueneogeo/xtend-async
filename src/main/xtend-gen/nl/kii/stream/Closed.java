package nl.kii.stream;

import nl.kii.stream.Entry;

/**
 * Indicates that the stream was closed and no more data will be passed
 */
@SuppressWarnings("all")
public class Closed<T extends Object> implements Entry<T> {
  public String toString() {
    return "closed stream";
  }
}
