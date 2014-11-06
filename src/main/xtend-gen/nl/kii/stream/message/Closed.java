package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Indicates that the stream was closed and no more data will be passed
 */
@SuppressWarnings("all")
public class Closed<I extends Object, O extends Object> implements Entry<I, O> {
  public String toString() {
    return "closed stream";
  }
}
