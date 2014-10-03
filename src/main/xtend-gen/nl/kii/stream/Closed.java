package nl.kii.stream;

import nl.kii.stream.Entry;

/**
 * Indicates that the stream was closed and no more data will be passed
 */
@SuppressWarnings("all")
public class Closed<R extends Object, T extends Object> implements Entry<R, T> {
  public String toString() {
    return "closed stream";
  }
}
