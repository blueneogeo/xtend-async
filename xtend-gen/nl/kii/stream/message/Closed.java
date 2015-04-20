package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Indicates that the stream was closed and no more data will be passed
 */
public class Closed<I extends java.lang.Object, O extends java.lang.Object> implements Entry<I, O> {
  public java.lang.String toString() {
    return "closed stream";
  }
}
