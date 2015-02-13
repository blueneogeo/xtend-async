package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Indicates that the stream encountered an error while processing information.
 */
@java.lang.SuppressWarnings("all")
public class Error<I extends java.lang.Object, O extends java.lang.Object> implements Entry<I, O> {
  public final I from;
  
  public final java.lang.Throwable error;
  
  public Error(final I from, final java.lang.Throwable error) {
    this.from = from;
    this.error = error;
  }
  
  @java.lang.Override
  public java.lang.String toString() {
    java.lang.String _message = this.error.getMessage();
    return ("error: " + _message);
  }
}
