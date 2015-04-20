package nl.kii.stream.message;

import nl.kii.stream.message.Entry;

/**
 * Indicates that the stream encountered an error while processing information.
 */
public class Error<I extends java.lang.Object, O extends java.lang.Object> implements Entry<I, O> {
  public final I from;
  
  public final /* Throwable */Object error;
  
  public Error(final I from, final /* Throwable */Object error) {
    this.from = from;
    this.error = error;
  }
  
  public Object toString() {
    throw new Error("Unresolved compilation problems:"
      + "\n+ cannot be resolved"
      + "\nmessage cannot be resolved");
  }
}
