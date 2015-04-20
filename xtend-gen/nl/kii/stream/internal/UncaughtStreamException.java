package nl.kii.stream.internal;

import nl.kii.stream.internal.StreamException;

/**
 * This is a StreamException that was not caught using a stream.onError handler,
 * and that should break out of the stream chain so surrounding code can catch
 * it, or it can be presented in the program output for debugging.
 */
public class UncaughtStreamException extends StreamException {
  /**
   * convert a streamexception into an uncaught streamexception
   */
  public UncaughtStreamException(final StreamException it) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field cause is undefined for the type UncaughtStreamException");
  }
  
  public UncaughtStreamException(final /* String */Object operation, final /* Object */Object value, final /* Throwable */Object cause) {
    super(operation, value, cause);
  }
}
